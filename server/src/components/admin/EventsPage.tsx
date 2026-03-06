'use client';

import { Plus, Edit, Trash2, Image as ImageIcon, X } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';

interface EventItem {
  id: number;
  name: string;
  description: string | null;
  imageUrl: string | null;
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
const STATUS_MAP: Record<string, string> = { scheduled: '예정', in_progress: '진행중', ended: '종료' };

const defaultForm = { programId: 0, name: '', description: '', imageUrl: '', type: 'raffle', startDate: '', endDate: '', reward: '', participantLimit: 100, status: 'scheduled' };

export default function EventsPage() {
  const [events, setEvents] = useState<EventItem[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [programs, setPrograms] = useState<{ id: number; name: string }[]>([]);
  const [form, setForm] = useState(defaultForm);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const fetchEvents = () => {
    fetch('/api/v1/admin/events').then((r) => r.json()).then((d) => { if (d.success) setEvents(d.data); });
  };

  useEffect(() => {
    fetchEvents();
    fetch('/api/v1/admin/programs').then((r) => r.json()).then((d) => {
      if (d.success) setPrograms(d.data.map((p: { id: number; name: string }) => ({ id: p.id, name: p.name })));
    });
  }, []);

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await fetch('/api/v1/admin/upload', { method: 'POST', body: formData });
      const json = await res.json();
      if (json.success) {
        setForm((prev) => ({ ...prev, imageUrl: json.data.imageUrl }));
      } else {
        alert(json.error?.message || '업로드 실패');
      }
    } catch {
      alert('업로드 중 오류가 발생했습니다');
    }
    setUploading(false);
  };

  const handleSubmit = async () => {
    if (!form.programId || !form.name || !form.type || !form.startDate || !form.endDate) {
      alert('소속 행사, 이벤트명, 유형, 시작일, 종료일은 필수입니다');
      return;
    }

    try {
      const url = editingId ? `/api/v1/admin/events/${editingId}` : '/api/v1/admin/events';
      const method = editingId ? 'PUT' : 'POST';
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (data.success) {
        resetForm();
        fetchEvents();
      } else {
        alert(`${editingId ? '수정' : '등록'} 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert(`${editingId ? '수정' : '등록'} 실패: 서버와 통신할 수 없습니다`);
    }
  };

  const handleEdit = (event: EventItem) => {
    setEditingId(event.id);
    setForm({
      programId: 0,
      name: event.name,
      description: event.description || '',
      imageUrl: event.imageUrl || '',
      type: event.type,
      startDate: event.startDate.slice(0, 10),
      endDate: event.endDate.slice(0, 10),
      reward: event.reward || '',
      participantLimit: event.participantLimit,
      status: event.status,
    });
    setShowForm(true);
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

  const resetForm = () => {
    setShowForm(false);
    setEditingId(null);
    setForm(defaultForm);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">추첨/선착순 이벤트 관리</h2>
          <p className="text-sm text-gray-500 mt-1">프로그램 내 추첨 및 선착순 이벤트를 관리합니다</p>
        </div>
        <button onClick={() => { resetForm(); setShowForm(true); }}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          이벤트 등록
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">{editingId ? '이벤트 수정' : '새 이벤트 등록'}</h3>
            <button onClick={resetForm} className="p-1 text-gray-400 hover:text-gray-600"><X className="w-5 h-5" /></button>
          </div>
          <div className="space-y-4">
            {!editingId && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">소속 행사</label>
                <select value={form.programId} onChange={(e) => setForm({ ...form, programId: Number(e.target.value) })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                  <option value={0}>행사를 선택하세요</option>
                  {programs.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
              </div>
            )}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">이벤트명</label>
              <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="이벤트 이름" className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">설명</label>
              <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
                placeholder="이벤트 설명" rows={3} className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">이미지</label>
              <div className="flex items-center gap-4">
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  onChange={handleUpload}
                  className="text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-medium file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                />
                {uploading && <span className="text-sm text-gray-500">업로드 중...</span>}
              </div>
              {form.imageUrl && (
                <div className="mt-3 relative inline-block">
                  <img src={form.imageUrl} alt="미리보기" className="h-32 rounded-lg object-cover border border-gray-200" />
                  <button onClick={() => setForm({ ...form, imageUrl: '' })}
                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-0.5 hover:bg-red-600">
                    <X className="w-3 h-3" />
                  </button>
                </div>
              )}
            </div>
            <div className="grid grid-cols-3 gap-4">
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
              {editingId && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">상태</label>
                  <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                    <option value="scheduled">예정</option>
                    <option value="in_progress">진행중</option>
                    <option value="ended">종료</option>
                  </select>
                </div>
              )}
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
            <div className="flex gap-2">
              <button onClick={handleSubmit}
                className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">{editingId ? '수정' : '등록'}</button>
              <button onClick={resetForm}
                className="px-6 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors">취소</button>
            </div>
          </div>
        </div>
      )}

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이미지</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이벤트명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">기간</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">보상</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">참여</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {events.map((event) => (
              <tr key={event.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  {event.imageUrl ? (
                    <img src={event.imageUrl} alt={event.name} className="h-14 w-24 rounded-lg object-cover border border-gray-200" />
                  ) : (
                    <div className="h-14 w-24 rounded-lg bg-gray-100 flex items-center justify-center">
                      <ImageIcon className="w-5 h-5 text-gray-300" />
                    </div>
                  )}
                </td>
                <td className="px-6 py-4">
                  <div className="flex flex-col gap-1">
                    <span className="text-sm font-medium text-gray-900">{event.name}</span>
                    <span className={`inline-flex w-fit px-2 py-0.5 text-xs rounded-full ${
                      event.type === 'raffle' ? 'bg-purple-100 text-purple-800' : 'bg-green-100 text-green-800'
                    }`}>
                      {TYPE_MAP[event.type] || event.type}
                    </span>
                    {event.description && (
                      <span className="text-xs text-gray-400 line-clamp-1">{event.description}</span>
                    )}
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
                  <span className={`inline-flex px-2 py-1 text-xs rounded-full font-medium ${
                    event.status === 'in_progress' ? 'bg-blue-50 text-blue-700' :
                    event.status === 'ended' ? 'bg-gray-100 text-gray-500' :
                    'bg-yellow-50 text-yellow-700'
                  }`}>
                    {STATUS_MAP[event.status] || event.status}
                  </span>
                </td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    <button onClick={() => handleEdit(event)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
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
              <tr><td colSpan={7} className="px-6 py-8 text-center text-sm text-gray-500">등록된 이벤트가 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
