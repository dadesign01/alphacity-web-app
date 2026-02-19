'use client';

import { Plus } from 'lucide-react';
import { useEffect, useState } from 'react';

interface StampItem {
  id: number;
  name: string;
  conditionType: string;
  conditionDetail: string | null;
  _count: { userStamps: number };
}

const CONDITION_MAP: Record<string, string> = {
  mission_complete: '미션 완료', event_participate: '이벤트 참여', place_visit: '장소 방문', quiz_correct: '퀴즈 정답',
};

export default function StampsPage() {
  const [stamps, setStamps] = useState<StampItem[]>([]);
  const [form, setForm] = useState({ name: '', conditionType: 'mission_complete', conditionDetail: '' });

  const fetchStamps = () => {
    fetch('/api/v1/admin/stamps').then((r) => r.json()).then((d) => { if (d.success) setStamps(d.data); });
  };

  useEffect(() => { fetchStamps(); }, []);

  const handleCreate = async () => {
    try {
      const res = await fetch('/api/v1/admin/stamps', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (data.success) {
        setForm({ name: '', conditionType: 'mission_complete', conditionDetail: '' });
        fetchStamps();
      } else {
        alert(`스탬프 등록 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('스탬프 등록 실패: 서버와 통신할 수 없습니다');
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">스탬프 관리</h2>
          <p className="text-sm text-gray-500 mt-1">스탬프 발급 조건을 설정합니다</p>
        </div>
        <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          스탬프 추가
        </button>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">스탬프 등록</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">스탬프명</label>
            <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
              placeholder="스탬프 이름을 입력하세요"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">발급 조건 설정</label>
            <select value={form.conditionType} onChange={(e) => setForm({ ...form, conditionType: e.target.value })}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              {Object.entries(CONDITION_MAP).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">상세 조건</label>
            <textarea value={form.conditionDetail} onChange={(e) => setForm({ ...form, conditionDetail: e.target.value })}
              placeholder="발급 조건에 대한 상세 설명을 입력하세요" rows={3}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
          <button onClick={handleCreate}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">등록</button>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">등록된 스탬프</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">스탬프명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">발급 조건</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">발급 수</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {stamps.map((stamp) => (
              <tr key={stamp.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">{stamp.name}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{CONDITION_MAP[stamp.conditionType] || stamp.conditionType}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{stamp._count.userStamps.toLocaleString()}개</td>
                <td className="px-6 py-4">
                  <button className="text-sm text-blue-600 hover:underline">수정</button>
                </td>
              </tr>
            ))}
            {stamps.length === 0 && (
              <tr><td colSpan={4} className="px-6 py-8 text-center text-sm text-gray-500">등록된 스탬프가 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
