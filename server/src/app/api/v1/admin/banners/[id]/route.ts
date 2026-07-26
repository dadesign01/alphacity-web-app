import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const body = await request.json();
    const { title, imageUrl, sortOrder, isActive, linkType, linkId } = body;

    const allowedLinkTypes = ['festival', 'program', 'event'];
    const normalizedLinkType =
      linkType && allowedLinkTypes.includes(linkType) ? linkType : null;
    const normalizedLinkId =
      normalizedLinkType && linkId != null ? Number(linkId) : null;

    const banner = await prisma.banner.update({
      where: { id: Number(id) },
      data: {
        ...(title !== undefined && { title }),
        ...(imageUrl !== undefined && { imageUrl }),
        ...(sortOrder !== undefined && { sortOrder }),
        ...(isActive !== undefined && { isActive }),
        ...(linkType !== undefined && { linkType: normalizedLinkType, linkId: normalizedLinkId }),
      },
    });

    return successResponse(banner, '배너가 수정되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}

export async function DELETE(
  _request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    await prisma.banner.delete({ where: { id: Number(id) } });
    return successResponse(null, '배너가 삭제되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
