'use client';

import { Plus, Edit, Trash2, Filter } from 'lucide-react';
import { useEffect, useState } from 'react';

interface Program {
  id: number;
  name: string;
  startDate: string;
  endDate: string;
  status: string;
  _count: { events: number };
}

const STATUS_MAP: Record<string, string> = {
  scheduled: '예정',
  in_progress: '진행중',
  ended: '종료',
};

export default function ProgramsPage() {
  const [programs, setPrograms] = useState<Program[]>([]);
  const [statusFilter, setStatusFilter] = useState('all');
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({ name: '', description: '', startDate: '', endDate: '' });

  const fetchPrograms = () => {
    fetch(`/api/v1/admin/programs?status=${statusFilter}`)
      .then((res) => res.json())
      .then((data) => { if (data.success) setPrograms(data.data); });
  };

  useEffect(() => { fetchPrograms(); }, [statusFilter]);

  const handleCreate = async () => {
    try {
      const res = await fetch('/api/v1/admin/programs', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (data.success) {
        setShowForm(false);
        setForm({ name: '', description: '', startDate: '', endDate: '' });
        setStatusFilter('all');
        fetchPrograms();
      } else {
        alert(`행사 등록 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('행사 등록 실패: 서버와 통신할 수 없습니다');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      const res = await fetch(`/api/v1/admin/programs/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        fetchPrograms();
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
          <h2 className="text-2xl font-semibold text-gray-900">행사 관리</h2>
          <p className="text-sm text-gray-500 mt-1">프로그램 행사를 등록하고 관리합니다</p>
        </div>
        <button
          onClick={() => setShowForm(!showForm)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="w-4 h-4" />
          행사 등록
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">새 행사 등록</h3>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">행사명</label>
              <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="행사 이름을 입력하세요"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">설명</label>
              <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
                placeholder="행사 설명을 입력하세요" rows={3}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
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
            <button onClick={handleCreate}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">등록</button>
          </div>
        </div>
      )}

      {/* 필터 */}
      <div className="mb-6 bg-white rounded-lg border border-gray-200 p-4">
        <div className="flex items-center gap-2 mb-3">
          <Filter className="w-4 h-4 text-gray-500" />
          <span className="text-sm font-medium text-gray-700">상태 필터</span>
        </div>
        <div className="flex gap-2">
          {['all', 'scheduled', 'in_progress', 'ended'].map((status) => (
            <button key={status} onClick={() => setStatusFilter(status)}
              className={`px-4 py-2 text-sm rounded-lg transition-colors ${
                statusFilter === status ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}>
              {status === 'all' ? '전체' : STATUS_MAP[status]}
            </button>
          ))}
        </div>
      </div>

      {/* 테이블 */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">행사명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">기간</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이벤트 수</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {programs.map((program) => (
              <tr key={program.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">{program.name}</td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {new Date(program.startDate).toLocaleDateString()} ~ {new Date(program.endDate).toLocaleDateString()}
                </td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-2 py-1 text-xs rounded-full ${
                    program.status === 'in_progress' ? 'bg-green-100 text-green-800'
                    : program.status === 'scheduled' ? 'bg-blue-100 text-blue-800'
                    : 'bg-gray-100 text-gray-800'
                  }`}>
                    {STATUS_MAP[program.status] || program.status}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">{program._count.events}개</td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    <button className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                      <Edit className="w-4 h-4" />
                    </button>
                    <button onClick={() => handleDelete(program.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {programs.length === 0 && (
              <tr><td colSpan={5} className="px-6 py-8 text-center text-sm text-gray-500">등록된 행사가 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
