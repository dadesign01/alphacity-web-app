'use client';

import { Plus, MapPin, Edit, Trash2, X, Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import dynamic from 'next/dynamic';

const PlacesMap = dynamic(() => import('./PlacesMap'), {
  ssr: false,
  loading: () => (
    <div className="w-full h-full min-h-[400px] bg-gray-100 rounded-lg flex items-center justify-center">
      <p className="text-sm text-gray-500">지도 로딩중...</p>
    </div>
  ),
});

interface Place {
  id: number;
  name: string;
  category: string;
  latitude: number;
  longitude: number;
  address: string | null;
  description: string | null;
  ttsText: string | null;
}

const CATEGORY_MAP: Record<string, string> = {
  food: '맛집', exhibition: '전시', seminar: '세미나', event: '이벤트',
};

const EMPTY_FORM = { name: '', category: 'food', latitude: '', longitude: '', address: '', description: '', ttsText: '' };

export default function PlacesPage() {
  const [places, setPlaces] = useState<Place[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(EMPTY_FORM);

  const fetchPlaces = () => {
    fetch('/api/v1/admin/places').then((r) => r.json()).then((d) => { if (d.success) setPlaces(d.data); });
  };

  useEffect(() => { fetchPlaces(); }, []);

  const openCreateForm = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setShowForm(true);
  };

  const openEditForm = (place: Place) => {
    setEditingId(place.id);
    setForm({
      name: place.name,
      category: place.category,
      latitude: String(place.latitude),
      longitude: String(place.longitude),
      address: place.address || '',
      description: place.description || '',
      ttsText: place.ttsText || '',
    });
    setShowForm(true);
  };

  const handleSubmit = async () => {
    const isEdit = editingId !== null;
    const url = isEdit ? `/api/v1/admin/places/${editingId}` : '/api/v1/admin/places';
    const method = isEdit ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...form, latitude: parseFloat(form.latitude), longitude: parseFloat(form.longitude) }),
      });
      const data = await res.json();
      if (data.success) {
        setShowForm(false);
        setEditingId(null);
        setForm(EMPTY_FORM);
        fetchPlaces();
      } else {
        alert(`${isEdit ? '수정' : '등록'} 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert(`${isEdit ? '수정' : '등록'} 실패: 서버와 통신할 수 없습니다`);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      const res = await fetch(`/api/v1/admin/places/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        fetchPlaces();
      } else {
        alert(`삭제 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('삭제 실패: 서버와 통신할 수 없습니다');
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">지도 / 장소 관리</h2>
          <p className="text-sm text-gray-500 mt-1">프로그램 장소를 등록하고 관리합니다</p>
        </div>
        <button onClick={openCreateForm}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          장소 등록
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              {editingId ? '장소 수정' : '새 장소 등록'}
            </h3>
            <button onClick={() => { setShowForm(false); setEditingId(null); }} className="text-gray-400 hover:text-gray-600">
              <X className="w-5 h-5" />
            </button>
          </div>
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">장소명</label>
                <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                  placeholder="장소 이름" className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">카테고리</label>
                <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                  {Object.entries(CATEGORY_MAP).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
                </select>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">위도</label>
                <input type="text" value={form.latitude} onChange={(e) => setForm({ ...form, latitude: e.target.value })}
                  placeholder="37.5665" className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">경도</label>
                <input type="text" value={form.longitude} onChange={(e) => setForm({ ...form, longitude: e.target.value })}
                  placeholder="126.978" className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">주소</label>
              <input type="text" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })}
                placeholder="주소" className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">설명</label>
              <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
                placeholder="장소 설명" rows={2}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">TTS 텍스트</label>
              <textarea value={form.ttsText} onChange={(e) => setForm({ ...form, ttsText: e.target.value })}
                placeholder="TTS로 안내할 텍스트" rows={2}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <button onClick={handleSubmit}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
              <Save className="w-4 h-4" />
              {editingId ? '수정' : '등록'}
            </button>
          </div>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">등록된 장소</h3>
          <div className="space-y-3">
            {places.map((place) => (
              <div key={place.id}
                className="flex items-center gap-3 p-4 rounded-lg border border-gray-200 hover:border-blue-300 transition-colors">
                <div className="p-2 bg-blue-100 rounded-lg">
                  <MapPin className="w-5 h-5 text-blue-600" />
                </div>
                <div className="flex-1">
                  <p className="text-sm font-medium text-gray-900">{place.name}</p>
                  <p className="text-xs text-gray-500">{CATEGORY_MAP[place.category] || place.category}</p>
                </div>
                <div className="flex gap-1">
                  <button onClick={() => openEditForm(place)}
                    className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                    <Edit className="w-4 h-4" />
                  </button>
                  <button onClick={() => handleDelete(place.id)}
                    className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}
            {places.length === 0 && <p className="text-sm text-gray-500 text-center py-4">등록된 장소가 없습니다</p>}
          </div>
        </div>

        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">지도</h3>
            {showForm && <p className="text-xs text-gray-500">지도를 클릭하면 좌표가 자동 입력됩니다</p>}
          </div>
          <div className="aspect-square rounded-lg overflow-hidden">
            <PlacesMap places={places} onMapClick={(lat, lng) => {
              if (showForm) {
                setForm((prev) => ({ ...prev, latitude: lat.toFixed(7), longitude: lng.toFixed(7) }));
              }
            }} />
          </div>
        </div>
      </div>
    </div>
  );
}
