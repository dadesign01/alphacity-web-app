import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { verifyToken } from '@/lib/auth';
import { successResponse, errorResponse } from '@/lib/api-response';
import bcrypt from 'bcryptjs';

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('authorization')?.replace('Bearer ', '');
    if (!token) return errorResponse('UNAUTHORIZED', '인증이 필요합니다', 401);

    const payload = await verifyToken(token);
    const userId = payload.userId as number;

    const body = await request.json();
    const { password } = body;

    if (!password) {
      return errorResponse('INVALID_INPUT', '비밀번호를 입력해주세요', 400);
    }

    const user = await prisma.user.findUnique({ where: { id: userId } });
    if (!user) return errorResponse('NOT_FOUND', '사용자를 찾을 수 없습니다', 404);

    // 소셜 로그인 사용자는 비밀번호 없음
    if (!user.passwordHash) {
      return errorResponse('NO_PASSWORD', '소셜 로그인 계정은 비밀번호를 변경할 수 없습니다', 400);
    }

    const isValid = await bcrypt.compare(password, user.passwordHash);
    if (!isValid) {
      return errorResponse('INVALID_PASSWORD', '현재 비밀번호가 일치하지 않습니다', 400);
    }

    return successResponse(null, '비밀번호가 확인되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
