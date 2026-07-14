#!/usr/bin/env python3
"""Enriches the network with real OSM road classes and traffic signals.

Fetches from Overpass (cached under .osmtmp/ if present, else queried):
  1. highway=traffic_signals nodes in the Joinville bbox
  2. the highway class of every way in the bbox

Produces:
  - traffic-state-service/.../city-network.json  → adds "class", recomputes "capacity"
  - traffic-state-service/.../traffic-signals.json → graph node ids that are signalised
  - frontend/public/signals.json → [[lng,lat], …] for map markers

Edge → way → class works because every edge id embeds its OSM way id (st-e<wayid>-<seg>).
Signals are matched to a graph node by id, or snapped to the nearest node within SNAP_M metres.
"""
import io
import json
import math
import os
import time
import urllib.parse
import urllib.request

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
CACHE = os.path.join(ROOT, ".osmtmp")
NET = os.path.join(ROOT, "traffic-state-service/src/main/resources/city-network.json")
GEOM = os.path.join(ROOT, "frontend/public/cityNetwork.json")
SIG_BACK = os.path.join(ROOT, "traffic-state-service/src/main/resources/traffic-signals.json")
SIG_FRONT = os.path.join(ROOT, "frontend/public/signals.json")

BBOX = "-26.31849,-48.91784,-26.26880,-48.83423"  # S,W,N,E of the network
SNAP_M = 55.0
MIRRORS = [
    "https://maps.mail.ru/osm/tools/overpass/api/interpreter",
    "https://overpass-api.de/api/interpreter",
    "https://overpass.kumi.systems/api/interpreter",
]

# Macroscopic capacity (veh/h) per OSM highway class — the calibrated flow table.
CAPACITY = {
    "motorway": 2600, "motorway_link": 1400,
    "trunk": 2200, "trunk_link": 1200,
    "primary": 1600, "primary_link": 1000,
    "secondary": 1200, "secondary_link": 900,
    "tertiary": 900, "tertiary_link": 700,
    "unclassified": 700, "residential": 600,
    "living_street": 400, "service": 400,
}
DEFAULT_CAP = 600
# Classes that act as traffic sources (through-roads that feed the city).
SOURCE_CLASSES = {"motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link"}


def load_json(path):
    with io.open(path, encoding="utf-8") as fh:
        return json.load(fh)


def overpass(query, cache_name):
    cached = os.path.join(CACHE, cache_name)
    if os.path.exists(cached):
        return load_json(cached)
    for url in MIRRORS:
        try:
            data = urllib.parse.urlencode({"data": query}).encode()
            with urllib.request.urlopen(url, data=data, timeout=200) as r:
                payload = json.loads(r.read().decode("utf-8"))
            if payload.get("elements") is not None:
                os.makedirs(CACHE, exist_ok=True)
                with io.open(cached, "w", encoding="utf-8") as fh:
                    json.dump(payload, fh)
                return payload
        except Exception as ex:  # noqa: BLE001 — try the next mirror
            print("  mirror failed (%s): %s" % (url, ex))
            time.sleep(8)
    raise RuntimeError("all Overpass mirrors failed for " + cache_name)


def way_id(edge_id):
    # st-e<wayid>-<seg>
    try:
        return int(edge_id.split("-")[1][1:])
    except (IndexError, ValueError):
        return None


def haversine_m(a, b):
    (lng1, lat1), (lng2, lat2) = a, b
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp, dl = math.radians(lat2 - lat1), math.radians(lng2 - lng1)
    h = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * 6_371_000 * math.asin(math.sqrt(h))


def main():
    net = load_json(NET)
    geom = {e["id"]: e["coords"] for e in load_json(GEOM)}

    ways = overpass('[out:json][timeout:180];way["highway"](%s);out tags;' % BBOX, "ways.json")
    signals = overpass('[out:json][timeout:60];node["highway"="traffic_signals"](%s);out;' % BBOX, "signals.json")

    way_class = {w["id"]: w.get("tags", {}).get("highway") for w in ways["elements"]}

    # 1. Enrich edges with class + class-derived capacity.
    import collections
    used = collections.Counter()
    for e in net:
        cls = way_class.get(way_id(e["id"])) or "residential"
        e["class"] = cls
        e["capacity"] = CAPACITY.get(cls, DEFAULT_CAP)
        used[cls] += 1
    print("edge classes:", dict(used.most_common()))

    # Node → coordinate (edge endpoints, from the geometry file) for snapping.
    node_coord = {}
    for e in net:
        c = geom.get(e["id"])
        if c and len(c) >= 2:
            node_coord.setdefault(e["nodeA"], c[0])
            node_coord.setdefault(e["nodeB"], c[-1])
    graph_nodes = set(node_coord)

    # 2. Map each signal to a graph node (exact id, else nearest within SNAP_M).
    signalised = set()
    front = []
    matched = snapped = 0
    for el in signals["elements"]:
        lng, lat = el["lon"], el["lat"]
        front.append([round(lng, 6), round(lat, 6)])
        nid = "n%d" % el["id"]
        if nid in graph_nodes:
            signalised.add(nid)
            matched += 1
            continue
        best, bestd = None, SNAP_M
        for node, coord in node_coord.items():
            d = haversine_m((lng, lat), coord)
            if d < bestd:
                best, bestd = node, d
        if best:
            signalised.add(best)
            snapped += 1
    print("signals: %d total · %d matched · %d snapped · %d unusable"
          % (len(front), matched, snapped, len(front) - matched - snapped))

    # 3. Write outputs.
    with io.open(NET, "w", encoding="utf-8") as fh:
        json.dump(net, fh, ensure_ascii=False, separators=(",", ":"))
    with io.open(SIG_BACK, "w", encoding="utf-8") as fh:
        json.dump(sorted(signalised), fh, separators=(",", ":"))
    with io.open(SIG_FRONT, "w", encoding="utf-8") as fh:
        json.dump(front, fh, separators=(",", ":"))
    print("wrote city-network.json, traffic-signals.json (%d nodes), signals.json (%d markers)"
          % (len(signalised), len(front)))
    # Emit the source-class set for reference (the engine uses the same list).
    print("source classes:", sorted(SOURCE_CLASSES))


if __name__ == "__main__":
    main()
