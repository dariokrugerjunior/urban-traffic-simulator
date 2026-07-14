import { useEffect, useRef } from 'react';

/** A point on the unit sphere. */
type Vec3 = { x: number; y: number; z: number };

const POINT_COUNT = 340;
const NEIGHBORS = 3; // edges per node → a road-like mesh wrapping the globe
const ARC_COUNT = 7;
const GREEN = '34,197,94';

/** Evenly distributes N points on a unit sphere (Fibonacci lattice). */
function fibonacciSphere(n: number): Vec3[] {
  const pts: Vec3[] = [];
  const golden = Math.PI * (3 - Math.sqrt(5));
  for (let i = 0; i < n; i++) {
    const y = 1 - (i / (n - 1)) * 2;
    const r = Math.sqrt(1 - y * y);
    const theta = i * golden;
    pts.push({ x: Math.cos(theta) * r, y, z: Math.sin(theta) * r });
  }
  return pts;
}

/** Connects each point to its nearest neighbors → the street mesh (computed once). */
function buildEdges(pts: Vec3[]): [number, number][] {
  const seen = new Set<string>();
  const edges: [number, number][] = [];
  for (let i = 0; i < pts.length; i++) {
    const pi = pts[i]!;
    const d = pts
      .map((p, j) => ({ j, dist: (p.x - pi.x) ** 2 + (p.y - pi.y) ** 2 + (p.z - pi.z) ** 2 }))
      .filter((o) => o.j !== i)
      .sort((a, b) => a.dist - b.dist)
      .slice(0, NEIGHBORS);
    for (const { j } of d) {
      const key = i < j ? `${i}-${j}` : `${j}-${i}`;
      if (!seen.has(key)) {
        seen.add(key);
        edges.push([i, j]);
      }
    }
  }
  return edges;
}

/** Great-circle-ish interpolation between two unit vectors. */
function slerp(a: Vec3, b: Vec3, t: number): Vec3 {
  const dot = Math.max(-1, Math.min(1, a.x * b.x + a.y * b.y + a.z * b.z));
  const omega = Math.acos(dot);
  if (omega < 1e-4) return a;
  const s = Math.sin(omega);
  const k0 = Math.sin((1 - t) * omega) / s;
  const k1 = Math.sin(t * omega) / s;
  return { x: a.x * k0 + b.x * k1, y: a.y * k0 + b.y * k1, z: a.z * k0 + b.z * k1 };
}

/**
 * A slowly rotating wireframe globe made of a street network — nodes linked by lines,
 * with glowing arcs and travelling "traffic" pulses. Pure canvas 2D, no dependencies.
 * Honors prefers-reduced-motion (renders a static globe).
 */
export function StreetGlobe() {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const points = fibonacciSphere(POINT_COUNT);
    const edges = buildEdges(points);
    // A fixed node we highlight as "Joinville".
    const joinvilleIdx = Math.floor(POINT_COUNT * 0.62);
    // Random great-circle arcs with travelling pulses.
    const arcs = Array.from({ length: ARC_COUNT }, (_, i) => ({
      a: (i * 53) % POINT_COUNT,
      b: (i * 97 + 31) % POINT_COUNT,
      speed: 0.12 + (i % 3) * 0.05,
      phase: (i / ARC_COUNT),
    }));

    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const projected: { sx: number; sy: number; depth: number }[] = new Array(POINT_COUNT);
    let raf = 0;
    let cx = 0;
    let cy = 0;
    let radius = 0;
    let dpr = 1;

    const resize = () => {
      dpr = Math.min(window.devicePixelRatio || 1, 2);
      const w = canvas.clientWidth;
      const h = canvas.clientHeight;
      canvas.width = Math.round(w * dpr);
      canvas.height = Math.round(h * dpr);
      cx = canvas.width / 2;
      cy = canvas.height / 2;
      radius = Math.min(canvas.width, canvas.height) * 0.42;
    };
    resize();
    const ro = new ResizeObserver(resize);
    ro.observe(canvas);

    const tilt = -0.42; // slight lean for a nicer viewing angle
    const cosT = Math.cos(tilt);
    const sinT = Math.sin(tilt);

    const render = (angle: number) => {
      const cosA = Math.cos(angle);
      const sinA = Math.sin(angle);
      // Rotate around Y, then tilt around X, then project.
      for (let i = 0; i < POINT_COUNT; i++) {
        const p = points[i]!;
        const rx = p.x * cosA + p.z * sinA;
        const rz = -p.x * sinA + p.z * cosA;
        const ry = p.y * cosT - rz * sinT;
        const rzz = p.y * sinT + rz * cosT;
        const persp = 1 / (1 - rzz * 0.12);
        projected[i] = {
          sx: cx + rx * radius * persp,
          sy: cy - ry * radius * persp,
          depth: (rzz + 1) / 2, // 0 back → 1 front
        };
      }

      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // Mesh edges — one cheap pass at low alpha.
      ctx.lineWidth = dpr;
      ctx.strokeStyle = `rgba(${GREEN},0.11)`;
      ctx.beginPath();
      for (const [i, j] of edges) {
        const a = projected[i]!;
        const b = projected[j]!;
        ctx.moveTo(a.sx, a.sy);
        ctx.lineTo(b.sx, b.sy);
      }
      ctx.stroke();

      // Nodes — brighter toward the front.
      for (let i = 0; i < POINT_COUNT; i++) {
        const p = projected[i]!;
        const alpha = 0.15 + p.depth * 0.65;
        const size = (0.6 + p.depth * 1.4) * dpr;
        ctx.fillStyle = `rgba(${GREEN},${alpha.toFixed(3)})`;
        ctx.fillRect(p.sx - size / 2, p.sy - size / 2, size, size);
      }

      // Glowing arcs with a travelling pulse.
      ctx.lineWidth = 1.4 * dpr;
      ctx.shadowColor = `rgba(${GREEN},0.9)`;
      for (const arc of arcs) {
        const A = points[arc.a]!;
        const B = points[arc.b]!;
        const seg = 22;
        ctx.beginPath();
        let firstDepth = 0;
        for (let s = 0; s <= seg; s++) {
          const v = slerp(A, B, s / seg);
          const rx = v.x * cosA + v.z * sinA;
          const rz = -v.x * sinA + v.z * cosA;
          const ry = v.y * cosT - rz * sinT;
          const rzz = v.y * sinT + rz * cosT;
          const persp = 1 / (1 - rzz * 0.12);
          const sx = cx + rx * radius * persp * 1.02;
          const sy = cy - ry * radius * persp * 1.02;
          if (s === 0) { ctx.moveTo(sx, sy); firstDepth = (rzz + 1) / 2; } else ctx.lineTo(sx, sy);
        }
        ctx.strokeStyle = `rgba(${GREEN},${(0.15 + firstDepth * 0.5).toFixed(3)})`;
        ctx.shadowBlur = 6 * dpr;
        ctx.stroke();
        ctx.shadowBlur = 0;

        // Pulse dot travelling along the arc.
        const t = (angle * arc.speed + arc.phase) % 1;
        const v = slerp(A, B, t);
        const rx = v.x * cosA + v.z * sinA;
        const rz = -v.x * sinA + v.z * cosA;
        const ry = v.y * cosT - rz * sinT;
        const rzz = v.y * sinT + rz * cosT;
        if (rzz > -0.2) {
          const persp = 1 / (1 - rzz * 0.12);
          const sx = cx + rx * radius * persp * 1.02;
          const sy = cy - ry * radius * persp * 1.02;
          ctx.beginPath();
          ctx.fillStyle = `rgba(180,255,210,${(0.5 + ((rzz + 1) / 2) * 0.5).toFixed(3)})`;
          ctx.shadowColor = `rgba(${GREEN},1)`;
          ctx.shadowBlur = 10 * dpr;
          ctx.arc(sx, sy, 2.2 * dpr, 0, Math.PI * 2);
          ctx.fill();
          ctx.shadowBlur = 0;
        }
      }

      // Joinville — a pulsing highlighted node when it faces us.
      const jp = projected[joinvilleIdx]!;
      if (jp.depth > 0.45) {
        const pulse = reduce ? 0.6 : 0.5 + Math.sin(angle * 3) * 0.5;
        ctx.beginPath();
        ctx.fillStyle = `rgba(190,255,215,0.95)`;
        ctx.shadowColor = `rgba(${GREEN},1)`;
        ctx.shadowBlur = 14 * dpr;
        ctx.arc(jp.sx, jp.sy, 3 * dpr, 0, Math.PI * 2);
        ctx.fill();
        ctx.beginPath();
        ctx.strokeStyle = `rgba(${GREEN},${(0.4 + pulse * 0.5).toFixed(3)})`;
        ctx.lineWidth = 1.4 * dpr;
        ctx.arc(jp.sx, jp.sy, (6 + pulse * 5) * dpr, 0, Math.PI * 2);
        ctx.stroke();
        ctx.shadowBlur = 0;
      }
    };

    if (reduce) {
      render(0.6);
    } else {
      const start = performance.now();
      const loop = (now: number) => {
        render(0.00025 * (now - start) + 0.6);
        raf = requestAnimationFrame(loop);
      };
      raf = requestAnimationFrame(loop);
    }

    return () => {
      cancelAnimationFrame(raf);
      ro.disconnect();
    };
  }, []);

  return <canvas ref={canvasRef} className="h-full w-full" aria-hidden="true" />;
}
