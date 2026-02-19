import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

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
        await prisma.admin.update({
          where: { id: admin.id },
          data: {
            ...(body.admin.name && { name: body.admin.name }),
            ...(body.admin.phone !== undefined && { phone: body.admin.phone }),
          },
        });
      }
    }

    return successResponse(null, '설정이 저장되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
