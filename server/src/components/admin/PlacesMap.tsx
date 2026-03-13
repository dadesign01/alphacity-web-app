'use client';

import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Leaflet 기본 마커 아이콘 수정 (Next.js에서 깨지는 문제 해결)
// eslint-disable-next-line @typescript-eslint/no-explicit-any
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
});

const CATEGORY_MAP: Record<string, string> = {
  food: '맛집', exhibition: '전시', seminar: '세미나', event: '이벤트',
};

interface Place {
  id: number;
  name: string;
  category: string;
  latitude: number;
  longitude: number;
  address: string | null;
}

interface PlacesMapProps {
  places: Place[];
  onMapClick?: (lat: number, lng: number) => void;
}

function MapClickHandler({ onMapClick }: { onMapClick?: (lat: number, lng: number) => void }) {
  useMapEvents({
    click(e) {
      onMapClick?.(e.latlng.lat, e.latlng.lng);
    },
  });
  return null;
}

export default function PlacesMap({ places, onMapClick }: PlacesMapProps) {
  const center: [number, number] = places.length > 0
    ? [Number(places[0].latitude), Number(places[0].longitude)]
    : [37.5665, 126.978]; // 서울 시청 기본값

  return (
    <MapContainer center={center} zoom={15} className="w-full h-full rounded-lg" style={{ minHeight: '400px' }}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <MapClickHandler onMapClick={onMapClick} />
      {places.map((place) => (
        <Marker key={place.id} position={[Number(place.latitude), Number(place.longitude)]}>
          <Popup>
            <div className="text-sm">
              <p className="font-semibold">{place.name}</p>
              <p className="text-gray-500">{CATEGORY_MAP[place.category] || place.category}</p>
              {place.address && <p className="text-gray-400 text-xs mt-1">{place.address}</p>}
            </div>
          </Popup>
        </Marker>
      ))}
    </MapContainer>
  );
}
