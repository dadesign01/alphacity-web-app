import { NextRequest } from 'next/server';
import { successResponse, errorResponse } from '@/lib/api-response';
import { writeFile, mkdir } from 'fs/promises';
import path from 'path';

export async function POST(request: NextRequest) {
  try {
    const formData = await request.formData();
    const file = formData.get('file') as File | null;

    if (!file) {
      return errorResponse('NO_FILE', '파일이 없습니다');
    }

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
    if (!allowedTypes.includes(file.type)) {
      return errorResponse('INVALID_TYPE', '허용되지 않는 파일 형식입니다 (jpg, png, webp, gif만 가능)');
    }

    if (file.size > 5 * 1024 * 1024) {
      return errorResponse('FILE_TOO_LARGE', '파일 크기는 5MB 이하여야 합니다');
    }

    const ext = file.name.split('.').pop() || 'jpg';
    const filename = `${Date.now()}_${Math.random().toString(36).slice(2, 8)}.${ext}`;

    const uploadDir = path.join(process.cwd(), 'public', 'uploads');
    await mkdir(uploadDir, { recursive: true });

    const buffer = Buffer.from(await file.arrayBuffer());
    await writeFile(path.join(uploadDir, filename), buffer);

    const imageUrl = `/uploads/${filename}`;

    return successResponse({ imageUrl }, '파일이 업로드되었습니다');
  } catch {
    return errorResponse('SERVER_ERROR', '파일 업로드 중 오류가 발생했습니다', 500);
  }
}
