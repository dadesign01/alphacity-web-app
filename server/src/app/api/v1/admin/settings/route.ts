import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';
import bcrypt from 'bcryptjs';

export async function GET() {
  try {
    const program = await prisma.program.findFirst({ orderBy: { createdAt: 'desc' } });
    const admin = await prisma.admin.findFirst();

    return successResponse({
      program: program
        ? { name: program.name, description: program.description, startDate: program.startDate, endDate: program.endDate, autoNotification: program.autoNotification, collectStats: program.collectStats }
        : null,
      admin: admin
        ? { name: admin.name, email: admin.email, phone: admin.phone }
        : null,
    });
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function PUT(request: NextRequest) {
  try {
    const body = await request.json();

    if (body.program) {
      const program = await prisma.program.findFirst({ orderBy: { createdAt: 'desc' } });
      if (program) {
        await prisma.program.update({
          where: { id: program.id },
          data: {
            ...(body.program.name && { name: body.program.name }),
            ...(body.program.description !== undefined && { description: body.program.description }),
            ...(body.program.startDate && { startDate: new Date(body.program.startDate) }),
            ...(body.program.endDate && { endDate: new Date(body.program.endDate) }),
            ...(body.program.autoNotification !== undefined && { autoNotification: body.program.autoNotification }),
            ...(body.program.collectStats !== undefined && { collectStats: body.program.collectStats }),
          },
        });
      }
    }

    if (body.admin) {
      const admin = await prisma.admin.findFirst();
      if (admin) {
        // 비밀번호 변경 요청 시 현재 비밀번호 검증
        if (body.admin.newPassword) {
          if (!body.admin.currentPassword) {
            return errorResponse('VALIDATION_ERROR', '현재 비밀번호를 입력해주세요', 400);
          }
          const isValid = await bcrypt.compare(body.admin.currentPassword, admin.password);
          if (!isValid) {
            return errorResponse('INVALID_PASSWORD', '현재 비밀번호가 일치하지 않습니다', 400);
          }
          if (body.admin.newPassword.length < 6) {
            return errorResponse('VALIDATION_ERROR', '새 비밀번호는 6자 이상이어야 합니다', 400);
          }
        }

        // 이메일 중복 확인
        if (body.admin.email && body.admin.email !== admin.email) {
          const existing = await prisma.admin.findUnique({ where: { email: body.admin.email } });
          if (existing) {
            return errorResponse('DUPLICATE_EMAIL', '이미 사용 중인 이메일입니다', 400);
          }
        }

        await prisma.admin.update({
          where: { id: admin.id },
          data: {
            ...(body.admin.name && { name: body.admin.name }),
            ...(body.admin.email && { email: body.admin.email }),
            ...(body.admin.phone !== undefined && { phone: body.admin.phone }),
            ...(body.admin.newPassword && { password: await bcrypt.hash(body.admin.newPassword, 10) }),
          },
        });
      }
    }

    return successResponse(null, '설정이 저장되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
