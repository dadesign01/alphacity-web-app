import { NextRequest } from 'next/server';
import { prisma } from '@/lib/prisma';
import { successResponse, errorResponse } from '@/lib/api-response';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = request.nextUrl;
    const lat = parseFloat(searchParams.get('lat') || '0');
    const lng = parseFloat(searchParams.get('lng') || '0');
    const radius = parseFloat(searchParams.get('radius') || '1'); // km

    if (!lat || !lng) {
      return errorResponse('INVALID_INPUT', '위도와 경도를 입력하세요');
    }

    const places = await prisma.place.findMany({
      where: { isActive: true },
      include: { missions: { select: { id: true, name: true, type: true } } },
    });

    // Haversine distance filter
    const nearbyPlaces = places
      .map((place) => {
        const dLat = ((Number(place.latitude) - lat) * Math.PI) / 180;
        const dLng = ((Number(place.longitude) - lng) * Math.PI) / 180;
        const a = Math.sin(dLat / 2) ** 2 + Math.cos((lat * Math.PI) / 180) * Math.cos((Number(place.latitude) * Math.PI) / 180) * Math.sin(dLng / 2) ** 2;
        const distance = 6371 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return { ...place, distance: Math.round(distance * 1000) }; // meters
      })
      .filter((p) => p.distance <= radius * 1000)
      .sort((a, b) => a.distance - b.distance);

    return successResponse(nearbyPlaces);
  } catch {
    return errorResponse('SERVER_ERROR', '서버 오류가 발생했습니다', 500);
  }
}
