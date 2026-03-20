'use client';

import { Edit2, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';

interface TermItem {
  id: number;
  type: string;
  title: string;
  content: string;
  version: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

const TYPE_MAP: Record<string, string> = {
  privacy: '개인정보처리방침',
  service: '이용약관',
};

export default function TermsPage() {
  const [terms, setTerms] = useState<TermItem[]>([]);
  const [filterType, setFilterType] = useState<string>('all');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState({
    type: 'privacy',
    title: '',
    content: '',
    version: '1.0',
    isActive: true,
  });

  const fetchTerms = () => {
    fetch('/api/v1/admin/terms')
      .then((r) => r.json())
      .then((d) => {
        if (d.success) setTerms(d.data);
      });
  };

  useEffect(() => {
    fetchTerms();
  }, []);

  const filteredTerms =
    filterType === 'all' ? terms : terms.filter((t) => t.type === filterType);

  const resetForm = () => {
    setForm({ type: 'privacy', title: '', content: '', version: '1.0', isActive: true });
    setEditingId(null);
    setShowForm(false);
  };

  const handleEdit = (term: TermItem) => {
    setForm({
      type: term.type,
      title: term.title,
      content: term.content,
      version: term.version,
      isActive: term.isActive,
    });
    setEditingId(term.id);
    setShowForm(true);
  };

  const handleSubmit = async () => {
    if (!form.title || !form.content) {
      alert('제목과 내용은 필수입니다');
      return;
    }

    try {
      const url = editingId
        ? `/api/v1/admin/terms/${editingId}`
        : '/api/v1/admin/terms';
      const method = editingId ? 'PUT' : 'POST';

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (data.success) {
        resetForm();
        fetchTerms();
        alert(data.message || '저장되었습니다');
      } else {
        alert(`저장 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('저장 실패: 서버와 통신할 수 없습니다');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
      const res = await fetch(`/api/v1/admin/terms/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        fetchTerms();
        alert('삭제되었습니다');
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
          <h2 className="text-2xl font-semibold text-gray-900">약관 관리</h2>
          <p className="text-sm text-gray-500 mt-1">
            개인정보처리방침 및 이용약관을 관리합니다
          </p>
        </div>
        <button
          onClick={() => {
            resetForm();
            setShowForm(true);
          }}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="w-4 h-4" />
          약관 추가
        </button>
      </div>

      {/* Filter */}
      <div className="flex gap-2 mb-4">
        {[
          { key: 'all', label: '전체' },
          { key: 'privacy', label: '개인정보처리방침' },
          { key: 'service', label: '이용약관' },
        ].map((f) => (
          <button
            key={f.key}
            onClick={() => setFilterType(f.key)}
            className={`px-4 py-1.5 rounded-full text-sm transition-colors ${
              filterType === f.key
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Form */}
      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <h3 className="text-lg font-medium text-gray-900 mb-4">
            {editingId ? '약관 수정' : '약관 추가'}
          </h3>
          <div className="space-y-4">
            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  유형
                </label>
                <select
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="privacy">개인정보처리방침</option>
                  <option value="service">이용약관</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  버전
                </label>
                <input
                  type="text"
                  value={form.version}
                  onChange={(e) => setForm({ ...form, version: e.target.value })}
                  placeholder="1.0"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div className="flex items-end">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={form.isActive}
                    onChange={(e) =>
                      setForm({ ...form, isActive: e.target.checked })
                    }
                    className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                  />
                  <span className="text-sm font-medium text-gray-700">활성화</span>
                </label>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                제목
              </label>
              <input
                type="text"
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                placeholder="약관 제목을 입력하세요"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                내용
              </label>
              <textarea
                value={form.content}
                onChange={(e) => setForm({ ...form, content: e.target.value })}
                rows={16}
                placeholder="약관 내용을 입력하세요"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
              />
            </div>
            <div className="flex justify-end gap-3">
              <button
                onClick={resetForm}
                className="px-4 py-2 text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                취소
              </button>
              <button
                onClick={handleSubmit}
                className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
              >
                {editingId ? '수정' : '등록'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* List */}
      <div className="bg-white rounded-lg border border-gray-200">
        <table className="w-full">
          <thead>
            <tr className="border-b border-gray-200 bg-gray-50">
              <th className="text-left px-6 py-3 text-sm font-medium text-gray-500">
                ID
              </th>
              <th className="text-left px-6 py-3 text-sm font-medium text-gray-500">
                유형
              </th>
              <th className="text-left px-6 py-3 text-sm font-medium text-gray-500">
                제목
              </th>
              <th className="text-left px-6 py-3 text-sm font-medium text-gray-500">
                버전
              </th>
              <th className="text-left px-6 py-3 text-sm font-medium text-gray-500">
                상태
              </th>
              <th className="text-left px-6 py-3 text-sm font-medium text-gray-500">
                수정일
              </th>
              <th className="text-left px-6 py-3 text-sm font-medium text-gray-500">
                관리
              </th>
            </tr>
          </thead>
          <tbody>
            {filteredTerms.length === 0 ? (
              <tr>
                <td
                  colSpan={7}
                  className="text-center py-8 text-sm text-gray-400"
                >
                  등록된 약관이 없습니다
                </td>
              </tr>
            ) : (
              filteredTerms.map((term) => (
                <tr
                  key={term.id}
                  className="border-b border-gray-100 hover:bg-gray-50"
                >
                  <td className="px-6 py-4 text-sm text-gray-900">{term.id}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        term.type === 'privacy'
                          ? 'bg-purple-100 text-purple-700'
                          : 'bg-blue-100 text-blue-700'
                      }`}
                    >
                      {TYPE_MAP[term.type] || term.type}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">
                    {term.title}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    v{term.version}
                  </td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${
                        term.isActive
                          ? 'bg-green-100 text-green-700'
                          : 'bg-gray-100 text-gray-500'
                      }`}
                    >
                      {term.isActive ? '활성' : '비활성'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    {new Date(term.updatedAt).toLocaleDateString('ko-KR')}
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleEdit(term)}
                        className="p-1.5 text-gray-400 hover:text-blue-600 transition-colors"
                        title="수정"
                      >
                        <Edit2 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(term.id)}
                        className="p-1.5 text-gray-400 hover:text-red-600 transition-colors"
                        title="삭제"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
