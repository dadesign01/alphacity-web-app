'use client';

import { useState, useEffect, useRef } from 'react';
import { Plus, Pencil, Trash2, GripVertical, Image as ImageIcon } from 'lucide-react';

interface Banner {
  id: number;
  title: string;
  imageUrl: string;
  sortOrder: number;
  isActive: boolean;
  createdAt: string;
}

export default function BannersPage() {
  const [banners, setBanners] = useState<Banner[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [title, setTitle] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [isActive, setIsActive] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [imgErrors, setImgErrors] = useState<Set<number>>(new Set());
  const fileInputRef = useRef<HTMLInputElement>(null);

  const fetchBanners = async () => {
    try {
      const res = await fetch('/api/v1/admin/banners');
      const json = await res.json();
      if (json.success) { setBanners(json.data); setImgErrors(new Set()); }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => { fetchBanners(); }, []);

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
        setImageUrl(json.data.imageUrl);
      } else {
        alert(json.error?.message || '업로드 실패');
      }
    } catch {
      alert('업로드 중 오류가 발생했습니다');
    }
    setUploading(false);
  };

  const handleSubmit = async () => {
    if (!title.trim() || !imageUrl.trim()) {
      alert('제목과 이미지는 필수입니다');
      return;
    }

    try {
      const url = editingId
        ? `/api/v1/admin/banners/${editingId}`
        : '/api/v1/admin/banners';
      const method = editingId ? 'PUT' : 'POST';

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title,
          imageUrl,
          isActive,
          sortOrder: editingId
            ? banners.find(b => b.id === editingId)?.sortOrder ?? 0
            : banners.length,
        }),
      });
      const json = await res.json();
      if (json.success) {
        resetForm();
        fetchBanners();
      } else {
        alert(json.error?.message || '저장 실패');
      }
    } catch {
      alert('저장 중 오류가 발생했습니다');
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('이 배너를 삭제하시겠습니까?')) return;
    try {
      const res = await fetch(`/api/v1/admin/banners/${id}`, { method: 'DELETE' });
      const json = await res.json();
      if (json.success) fetchBanners();
    } catch {
      alert('삭제 중 오류가 발생했습니다');
    }
  };

  const handleEdit = (banner: Banner) => {
    setEditingId(banner.id);
    setTitle(banner.title);
    setImageUrl(banner.imageUrl);
    setIsActive(banner.isActive);
    setShowForm(true);
  };

  const resetForm = () => {
    setShowForm(false);
    setEditingId(null);
    setTitle('');
    setImageUrl('');
    setIsActive(true);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">배너 관리</h1>
          <p className="text-sm text-gray-500 mt-1">메인 화면 배너 캐러셀에 표시될 이미지를 관리합니다</p>
        </div>
        <button
          onClick={() => { resetForm(); setShowForm(true); }}
          className="flex items-center gap-2 bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700 text-sm"
        >
          <Plus className="w-4 h-4" />
          배너 추가
        </button>
      </div>

      {/* Form */}
      {showForm && (
        <div className="bg-white border border-gray-200 rounded-xl p-6 mb-6">
          <h2 className="text-lg font-medium text-gray-900 mb-4">
            {editingId ? '배너 수정' : '새 배너 등록'}
          </h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">제목</label>
              <input
                type="text"
                value={title}
                onChange={e => setTitle(e.target.value)}
                placeholder="배너 제목"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">이미지</label>
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
              {imageUrl && (
                <div className="mt-3">
                  <img
                    src={imageUrl}
                    alt="미리보기"
                    className="h-32 rounded-lg object-cover border border-gray-200"
                  />
                </div>
              )}
            </div>

            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                id="isActive"
                checked={isActive}
                onChange={e => setIsActive(e.target.checked)}
                className="rounded border-gray-300"
              />
              <label htmlFor="isActive" className="text-sm text-gray-700">활성화</label>
            </div>

            <div className="flex gap-2">
              <button
                onClick={handleSubmit}
                className="bg-blue-600 text-white px-6 py-2 rounded-lg hover:bg-blue-700 text-sm"
              >
                {editingId ? '수정' : '등록'}
              </button>
              <button
                onClick={resetForm}
                className="bg-gray-100 text-gray-700 px-6 py-2 rounded-lg hover:bg-gray-200 text-sm"
              >
                취소
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Banner List */}
      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        {banners.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-gray-400">
            <ImageIcon className="w-12 h-12 mb-3" />
            <p className="text-sm">등록된 배너가 없습니다</p>
          </div>
        ) : (
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">순서</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">이미지</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">제목</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">상태</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase">관리</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {banners.map((banner, index) => (
                <tr key={banner.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2 text-gray-400">
                      <GripVertical className="w-4 h-4" />
                      <span className="text-sm">{index + 1}</span>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    {imgErrors.has(banner.id) ? (
                      <div className="h-16 w-28 rounded-lg border border-gray-200 bg-gray-100 flex items-center justify-center">
                        <ImageIcon className="w-5 h-5 text-gray-400" />
                      </div>
                    ) : (
                      <img
                        src={banner.imageUrl}
                        alt={banner.title}
                        className="h-16 w-28 rounded-lg object-cover border border-gray-200"
                        onError={() => setImgErrors(prev => new Set(prev).add(banner.id))}
                      />
                    )}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-900">{banner.title}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex px-2 py-1 text-xs rounded-full font-medium ${
                      banner.isActive
                        ? 'bg-green-50 text-green-700'
                        : 'bg-gray-100 text-gray-500'
                    }`}>
                      {banner.isActive ? '활성' : '비활성'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => handleEdit(banner)}
                        className="p-1.5 text-gray-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg"
                      >
                        <Pencil className="w-4 h-4" />
                      </button>
                      <button
                        onClick={() => handleDelete(banner.id)}
                        className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
