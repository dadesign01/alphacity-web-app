import { NextResponse } from 'next/server';

export function successResponse(data: unknown, message?: string) {
  return NextResponse.json({ success: true, data, message });
}

export function errorResponse(code: string, message: string, status = 400) {
  return NextResponse.json(
    { success: false, error: { code, message } },
    { status }
  );
}
