'use client';

import { Plus, Edit, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';

interface EventItem {
  id: number;
  name: string;
  type: string;
  startDate: string;
  endDate: string;
  reward: string | null;
  participantLimit: number;
  status: string;
  program: { name: string };
  _count: { participants: number };
}

const TYPE_MAP: Record<string, string> = { raffle: '추첨', first_come: '선착순' };

export default function EventsPage() {
  const [events, setEvents] = useState<EventItem[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [programs, setPrograms] = useState<{ id: number; name: string }[]>([]);
  const [form, setForm] = useState({ programId: 0, name: '', type: 'raffle', startDate: '', endDate: '', reward: '', participantLimit: 100 });

  const fetchEvents = () => {
    fetch('/api/v1/admin/events').then((r) => r.json()).then((d) => { if (d.success) setEvents(d.data); });
  };

  useEffect(() => {
    fetchEvents();
    fetch('/api/v1/admin/programs').then((r) => r.json()).then((d) => {
      if (d.success) setPrograms(d.data.map((p: { id: number; name: string }) => ({ id: p.id, name: p.name })));
    });
  }, []);

  const handleCreate = async () => {
    try {
      const res = await fetch('/api/v1/admin/events', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (data.success) {
        setShowForm(false);
        setForm({ programId: 0, name: '', type: 'raffle', startDate: '', endDate: '', reward: '', participantLimit: 100 });
        fetchEvents();
      } else {
        alert(`이벤트 등록 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('이벤트 등록 실패: 서버와 통신할 수 없습니다');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      const res = await fetch(`/api/v1/admin/events/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        fetchEvents();
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
          <h2 className="text-2xl font-semibold text-gray-900">추첨/선착순 이벤트 관리</h2>
          <p className="text-sm text-gray-500 mt-1">프로그램 내 추첨 및 선착순 이벤트를 관리합니다</p>
        </div>
        <button onClick={() => setShowForm(!showForm)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          이벤트 등록
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">새 이벤트 등록</h3>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">소속 행사</label>
              <select value={form.programId} onChange={(e) => setForm({ ...form, programId: Number(e.target.value) })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                <option value={0}>행사를 선택하세요</option>
                {programs.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">이벤트명</label>
              <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="이벤트 이름" className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">유형</label>
                <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                  <option value="raffle">추첨</option>
                  <option value="first_come">선착순</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">참여 제한</label>
                <input type="number" value={form.participantLimit} onChange={(e) => setForm({ ...form, participantLimit: Number(e.target.value) })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">시작일</label>
                <input type="date" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">종료일</label>
                <input type="date" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">보상</label>
              <input type="text" value={form.reward} onChange={(e) => setForm({ ...form, reward: e.target.value })}
                placeholder="보상 내용" className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <button onClick={handleCreate}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">등록</button>
          </div>
        </div>
      )}

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이벤트명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이벤트 기간</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">보상</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">참여 제한 수</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {events.map((event) => (
              <tr key={event.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div className="flex flex-col gap-1">
                    <span className="text-sm font-medium text-gray-900">{event.name}</span>
                    <span className={`inline-flex w-fit px-2 py-0.5 text-xs rounded-full ${
                      event.type === 'raffle' ? 'bg-purple-100 text-purple-800' : 'bg-green-100 text-green-800'
                    }`}>
                      {TYPE_MAP[event.type] || event.type}
                    </span>
                  </div>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {new Date(event.startDate).toLocaleDateString()} ~ {new Date(event.endDate).toLocaleDateString()}
                </td>
                <td className="px-6 py-4 text-sm font-medium text-gray-900">{event.reward || '-'}</td>
                <td className="px-6 py-4">
                  <div className="flex flex-col gap-1">
                    <span className="text-sm text-gray-900">
                      {event._count.participants} / {event.participantLimit}명
                    </span>
                    <div className="w-full bg-gray-200 rounded-full h-1.5">
                      <div
                        className={`h-1.5 rounded-full ${
                          event._count.participants >= event.participantLimit ? 'bg-red-500' : 'bg-blue-600'
                        }`}
                        style={{ width: `${Math.min((event._count.participants / (event.participantLimit || 1)) * 100, 100)}%` }}
                      ></div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    <button className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                      <Edit className="w-4 h-4" />
                    </button>
                    <button onClick={() => handleDelete(event.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {events.length === 0 && (
              <tr><td colSpan={5} className="px-6 py-8 text-center text-sm text-gray-500">등록된 이벤트가 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
