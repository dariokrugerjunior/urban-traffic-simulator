#!/usr/bin/env python3
"""Builds routing-service/src/main/resources/routing-graph.json.

Joins, on edge id, the two existing 2887-edge data files:
  - traffic-state-service/src/main/resources/city-network.json  (nodeA, nodeB, oneway, name)
  - frontend/public/cityNetwork.json                            (coords polyline [lng, lat])

Output edge: {id, name, nodeA, nodeB, oneway, weightMeters} where weightMeters is the
haversine length of the polyline. Reproducible: re-run to regenerate the committed JSON.
"""
import io
import json
import math
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TS = os.path.join(ROOT, "traffic-state-service/src/main/resources/city-network.json")
FE = os.path.join(ROOT, "frontend/public/cityNetwork.json")
OUT = os.path.join(ROOT, "routing-service/src/main/resources/routing-graph.json")

EARTH_M = 6_371_000.0


def load(path):
    with io.open(path, encoding="utf-8") as fh:
        return json.load(fh)


def haversine(a, b):
    lng1, lat1 = a
    lng2, lat2 = b
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp = math.radians(lat2 - lat1)
    dl = math.radians(lng2 - lng1)
    h = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * EARTH_M * math.asin(math.sqrt(h))


def length_m(coords):
    total = 0.0
    for i in range(1, len(coords)):
        total += haversine(coords[i - 1], coords[i])
    return total


def main():
    topo = {e["id"]: e for e in load(TS)}
    geom = {e["id"]: e for e in load(FE)}

    edges = []
    for eid, t in topo.items():
        g = geom.get(eid)
        if not g or len(g.get("coords", [])) < 2:
            continue
        meters = round(length_m(g["coords"]), 2)
        edges.append({
            "id": eid,
            "name": t.get("name") or "Rua sem nome",
            "nodeA": t["nodeA"],
            "nodeB": t["nodeB"],
            "oneway": bool(t.get("oneway", False)),
            "weightMeters": max(1.0, meters),  # never zero — Dijkstra needs positive weights
        })

    with io.open(OUT, "w", encoding="utf-8") as fh:
        json.dump(edges, fh, ensure_ascii=False, separators=(",", ":"))
    print("wrote %d edges to %s" % (len(edges), OUT))


if __name__ == "__main__":
    main()
