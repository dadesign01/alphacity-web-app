'use client';

import { Plus } from 'lucide-react';
import { useEffect, useState } from 'react';

interface Mission {
  id: number;
  name: string;
  type: string;
  place: { name: string } | null;
  question: string | null;
  stayMinutes: number | null;
  _count: { completions: number };
}

interface Place {
  id: number;
  name: string;
}

const TYPE_MAP: Record<string, string> = { quiz: '퀴즈', location_auth: '위치 인증', stay_time: '체류시간' };

export default function MissionsPage() {
  const [missions, setMissions] = useState<Mission[]>([]);
  const [places, setPlaces] = useState<Place[]>([]);
  const [missionType, setMissionType] = useState('quiz');
  const [form, setForm] = useState({ name: '', placeId: 0, question: '', answer: '', stayMinutes: 5 });

  const fetchMissions = () => {
    fetch('/api/v1/admin/missions').then((r) => r.json()).then((d) => { if (d.success) setMissions(d.data); });
  };

  useEffect(() => {
    fetchMissions();
    fetch('/api/v1/admin/places').then((r) => r.json()).then((d) => {
      if (d.success) setPlaces(d.data.map((p: Place) => ({ id: p.id, name: p.name })));
    });
  }, []);

  const handleCreate = async () => {
    try {
      const res = await fetch('/api/v1/admin/missions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: form.name,
          type: missionType === 'location' ? 'location_auth' : missionType === 'stay' ? 'stay_time' : 'quiz',
          placeId: form.placeId || null,
          question: missionType === 'quiz' ? form.question : null,
          answer: missionType === 'quiz' ? form.answer : null,
          stayMinutes: missionType === 'stay' ? form.stayMinutes : null,
        }),
      });
      const data = await res.json();
      if (data.success) {
        setForm({ name: '', placeId: 0, question: '', answer: '', stayMinutes: 5 });
        fetchMissions();
      } else {
        alert(`미션 등록 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('미션 등록 실패: 서버와 통신할 수 없습니다');
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">미션 관리</h2>
          <p className="text-sm text-gray-500 mt-1">사용자 미션을 생성하고 관리합니다</p>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">새 미션 등록</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">미션 유형 선택</label>
            <div className="flex gap-3">
              {[{ value: 'quiz', label: '퀴즈' }, { value: 'location', label: '위치 인증' }, { value: 'stay', label: '체류시간' }].map((type) => (
                <button key={type.value} onClick={() => setMissionType(type.value)}
                  className={`px-4 py-2 text-sm rounded-lg transition-colors ${
                    missionType === type.value ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}>
                  {type.label}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">미션명</label>
            <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
              placeholder="미션 이름을 입력하세요"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>

          {missionType === 'quiz' && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">퀴즈 질문</label>
                <input type="text" value={form.question} onChange={(e) => setForm({ ...form, question: e.target.value })}
                  placeholder="퀴즈 질문을 입력하세요"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">정답</label>
                <input type="text" value={form.answer} onChange={(e) => setForm({ ...form, answer: e.target.value })}
                  placeholder="정답을 입력하세요"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            </>
          )}

          {missionType === 'location' && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">인증 장소</label>
              <select value={form.placeId} onChange={(e) => setForm({ ...form, placeId: Number(e.target.value) })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                <option value={0}>장소를 선택하세요</option>
                {places.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
            </div>
          )}

          {missionType === 'stay' && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">체류 장소</label>
                <select value={form.placeId} onChange={(e) => setForm({ ...form, placeId: Number(e.target.value) })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                  <option value={0}>장소를 선택하세요</option>
                  {places.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">인증 시간 설정</label>
                <div className="flex items-center gap-3">
                  <input type="number" value={form.stayMinutes} onChange={(e) => setForm({ ...form, stayMinutes: Number(e.target.value) })}
                    min="1" max="60"
                    className="w-24 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                  <span className="text-sm text-gray-600">분</span>
                </div>
                <p className="text-xs text-gray-500 mt-1">사용자가 해당 장소에 머물러야 하는 최소 시간</p>
              </div>
            </>
          )}

          <button onClick={handleCreate}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            <Plus className="w-4 h-4" />
            미션 등록
          </button>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">등록된 미션</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">미션명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">유형</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상세</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">완료 수</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {missions.map((mission) => (
              <tr key={mission.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">{mission.name}</td>
                <td className="px-6 py-4">
                  <span className="inline-flex px-2 py-1 text-xs rounded-full bg-purple-100 text-purple-800">
                    {TYPE_MAP[mission.type] || mission.type}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {mission.question || (mission.place?.name ? `${mission.place.name}${mission.stayMinutes ? ` / ${mission.stayMinutes}분` : ''}` : '-')}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">{mission._count.completions}건</td>
                <td className="px-6 py-4">
                  <button className="text-sm text-blue-600 hover:underline">수정</button>
                </td>
              </tr>
            ))}
            {missions.length === 0 && (
              <tr><td colSpan={5} className="px-6 py-8 text-center text-sm text-gray-500">등록된 미션이 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
