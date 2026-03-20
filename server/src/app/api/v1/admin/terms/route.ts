import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET() {
  try {
    const terms = await prisma.term.findMany({
      orderBy: { updatedAt: 'desc' },
    });
    return successResponse(terms);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { type, title, content, version, isActive } = body;

    if (!type || !title || !content) {
      return errorResponse('INVALID_INPUT', '유형, 제목, 내용은 필수입니다');
    }

    const term = await prisma.term.create({
      data: {
        type,
        title,
        content,
        version: version || '1.0',
        isActive: isActive !== undefined ? isActive : true,
      },
    });

    return successResponse(term, '약관이 등록되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
