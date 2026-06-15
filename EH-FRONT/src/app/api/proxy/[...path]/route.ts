import { NextRequest, NextResponse } from "next/server";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

async function proxy(request: NextRequest, context: { params: Promise<{ path: string[] }> }) {
  const { path } = await context.params;
  const targetUrl = new URL(`/api/v1/${path.join("/")}`, API_URL);
  request.nextUrl.searchParams.forEach((value, key) => targetUrl.searchParams.set(key, value));

  const body = ["GET", "HEAD"].includes(request.method) ? undefined : await request.text();
  const response = await fetch(targetUrl, {
    body,
    headers: request.headers,
    method: request.method,
  });

  return new NextResponse(response.body, {
    headers: response.headers,
    status: response.status,
  });
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
