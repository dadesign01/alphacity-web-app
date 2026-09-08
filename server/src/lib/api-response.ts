import { NextResponse } from 'next/server';

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization',
};

export function successResponse(data: unknown, message?: string) {
  return NextResponse.json(
    { success: true, data, message },
    {
      headers: CORS_HEADERS,
    }
  );
}

export function errorResponse(
  code: string,
  message: string,
  status = 400
) {
  return NextResponse.json(
    { success: false, error: { code, message } },
    {
      status,
      headers: CORS_HEADERS,
    }
  );
}