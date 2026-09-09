import { NextResponse } from 'next/server';
import { prisma } from '@/lib/prisma';

export async function GET() {
  try {
    await prisma.$queryRaw`SELECT 1`;

    return NextResponse.json({
      success: true,
      message: 'DB 연결 성공',
    });
  } catch (error) {
    console.error('[DB TEST ERROR]', error);

    return NextResponse.json(
      {
        success: false,
        message: 'DB 연결 실패',
        error: error instanceof Error ? error.message : String(error),
      },
      { status: 500 }
    );
  }
}