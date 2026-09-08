'use client';

import { Plus, Upload, Edit, Trash2, X, Save, QrCode } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';

interface StampItem {
  id: number;
  festivalId: number;
  festival?: { id: number; name: string };
  name: string;
  conditionType: string;
  conditionDetail: string | null;
  imageUrl: string | null;
  description: string | null;
  operatingHours: string | null;
  qrCode: string | null;
  isActive: boolean;
  program: { id: number; name: string } | null;
  place: { id: number; name: string } | null;
  _count: { userStamps: number };
}

interface OptionItem {
  id: number;
  name: string;
}

interface FestivalOption {
  id: number;
  name: string;
}

const CONDITION_MAP: Record<string, string> = {
  mission_complete: '미션 완료',
  event_participate: '이벤트 참여',
  place_visit: '장소 방문',
  quiz_correct: '퀴즈 정답',
};

const EMPTY_FORM = {
  festivalId: 0,
  name: '',
  conditionType: 'mission_complete',
  conditionDetail: '',
  programId: 0,
  placeId: 0,
  description: '',
  operatingHours: '',
  isActive: true,
  useQrCode: false,
};

export default function StampsPage() {
  const [stamps, setStamps] = useState<StampItem[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(EMPTY_FORM);

  const [festivals, setFestivals] = useState<FestivalOption[]>([]);
  const [festivalFilter, setFestivalFilter] = useState('all');

  const [programs, setPrograms] = useState<OptionItem[]>([]);
  const [places, setPlaces] = useState<OptionItem[]>([]);

  const [imageUrl, setImageUrl] = useState('');
  const [uploading, setUploading] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const fetchStamps = () => {
    const params = new URLSearchParams();

    if (festivalFilter !== 'all') {
      params.set('festivalId', festivalFilter);
    }

    fetch(`/api/v1/admin/stamps?${params}`)
      .then((r) => r.json())
      .then((d) => {
        if (d.success) {
          setStamps(d.data);
        }
      });
  };

  useEffect(() => {
    fetch('/api/v1/admin/festivals')
      .then((r) => r.json())
      .then((d) => {
        if (d.success) {
          setFestivals(
            d.data.map((f: FestivalOption) => ({
              id: f.id,
              name: f.name,
            })),
          );
        }
      });

    fetch('/api/v1/admin/programs')
      .then((r) => r.json())
      .then((d) => {
        if (d.success) {
          setPrograms(
            d.data.map((p: OptionItem) => ({
              id: p.id,
              name: p.name,
            })),
          );
        }
      });

    fetch('/api/v1/admin/places')
      .then((r) => r.json())
      .then((d) => {
        if (d.success) {
          setPlaces(
            d.data.map((p: OptionItem) => ({
              id: p.id,
              name: p.name,
            })),
          );
        }
      });
  }, []);

  useEffect(() => {
    fetchStamps();
  }, [festivalFilter]);

  const handleUpload = async (
    e: React.ChangeEvent<HTMLInputElement>,
  ) => {
    const file = e.target.files?.[0];

    if (!file) return;

    setUploading(true);

    try {
      const formData = new FormData();
      formData.append('file', file);

      const res = await fetch('/api/v1/admin/upload', {
        method: 'POST',
        body: formData,
      });

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

  const openCreateForm = () => {
    setEditingId(null);

    setForm({
      ...EMPTY_FORM,
      festivalId: festivals[0]?.id ?? 0,
    });

    setImageUrl('');

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }

    setShowForm(true);
  };

  const openEditForm = (stamp: StampItem) => {
    setEditingId(stamp.id);

    setForm({
      festivalId: stamp.festivalId,
      name: stamp.name,
      conditionType: stamp.conditionType,
      conditionDetail: stamp.conditionDetail || '',
      programId: stamp.program?.id || 0,
      placeId: stamp.place?.id || 0,
      description: stamp.description || '',
      operatingHours: stamp.operatingHours || '',
      isActive: stamp.isActive,

      // 수정에서는 기존 QR을 변경하지 않기 때문에
      // 현재 QR 존재 여부만 화면 표시용으로 사용
      useQrCode: !!stamp.qrCode,
    });

    setImageUrl(stamp.imageUrl || '');

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }

    setShowForm(true);
  };

  const handleSubmit = async () => {
    if (!form.festivalId) {
      alert('축제를 선택하세요');
      return;
    }

    if (!form.name.trim()) {
      alert('스탬프명을 입력하세요');
      return;
    }

    // 신규 등록이고 QR을 사용하는 경우 프로그램 필수
    if (!editingId && form.useQrCode && !form.programId) {
      alert('QR을 사용하는 경우 행사(프로그램)를 선택하세요');
      return;
    }

    const isEdit = editingId !== null;

    const url = isEdit
      ? `/api/v1/admin/stamps/${editingId}`
      : '/api/v1/admin/stamps';

    const method = isEdit ? 'PUT' : 'POST';

    try {
      const requestBody = {
        ...form,
        name: form.name.trim(),
        programId: form.programId || null,
        placeId: form.placeId || null,
        imageUrl: imageUrl || null,
        description: form.description.trim() || null,
        operatingHours: form.operatingHours.trim() || null,
      };

      const res = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      const data = await res.json();

      if (data.success) {
        setShowForm(false);
        setEditingId(null);
        setForm(EMPTY_FORM);
        setImageUrl('');
        fetchStamps();
      } else {
        alert(
          `${isEdit ? '수정' : '등록'} 실패: ${
            data.error?.message || '알 수 없는 오류'
          }`,
        );
      }
    } catch {
      alert(
        `${isEdit ? '수정' : '등록'} 실패: 서버와 통신할 수 없습니다`,
      );
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
      const res = await fetch(
        `/api/v1/admin/stamps/${id}`,
        {
          method: 'DELETE',
        },
      );

      const data = await res.json();

      if (data.success) {
        fetchStamps();
      } else {
        alert(
          `삭제 실패: ${
            data.error?.message || '알 수 없는 오류'
          }`,
        );
      }
    } catch {
      alert('삭제 실패: 서버와 통신할 수 없습니다');
    }
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
    setForm(EMPTY_FORM);
    setImageUrl('');

    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <div className="p-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">
            스탬프 관리
          </h2>

          <p className="text-sm text-gray-500 mt-1">
            스탬프 발급 조건과 운영 정보를 관리합니다
          </p>
        </div>

        <div className="flex items-center gap-3">
          <select
            value={festivalFilter}
            onChange={(e) =>
              setFestivalFilter(e.target.value)
            }
            className="px-3 py-2 text-sm border border-gray-300 rounded-lg bg-white"
          >
            <option value="all">전체 축제</option>

            {festivals.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </select>

          <button
            onClick={openCreateForm}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Plus className="w-4 h-4" />
            스탬프 추가
          </button>
        </div>
      </div>

      {/* Form */}
      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              {editingId ? '스탬프 수정' : '스탬프 등록'}
            </h3>

            <button
              onClick={closeForm}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <div className="space-y-4">
            {/* 축제 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                축제 *
              </label>

              <select
                value={form.festivalId}
                onChange={(e) =>
                  setForm({
                    ...form,
                    festivalId: Number(e.target.value),
                  })
                }
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value={0}>
                  축제를 선택하세요
                </option>

                {festivals.map((f) => (
                  <option key={f.id} value={f.id}>
                    {f.name}
                  </option>
                ))}
              </select>
            </div>

            {/* 스탬프명 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                스탬프명 *
              </label>

              <input
                type="text"
                value={form.name}
                onChange={(e) =>
                  setForm({
                    ...form,
                    name: e.target.value,
                  })
                }
                placeholder="스탬프 이름을 입력하세요"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            {/* 설명 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                스탬프 설명
              </label>

              <textarea
                value={form.description}
                onChange={(e) =>
                  setForm({
                    ...form,
                    description: e.target.value,
                  })
                }
                placeholder="스탬프에 대한 설명을 입력하세요"
                rows={3}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            {/* 프로그램 / 장소 */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  매칭할 행사(프로그램)
                  {(!editingId && form.useQrCode) && (
                    <span className="text-red-500 ml-1">*</span>
                  )}
                </label>

                <select
                  value={form.programId}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      programId: Number(e.target.value),
                    })
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value={0}>
                    {form.useQrCode
                      ? '행사(프로그램)를 선택하세요'
                      : '선택 안 함'}
                  </option>

                  {programs.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>

                {!editingId && form.useQrCode && (
                  <p className="text-xs text-gray-500 mt-1">
                    QR을 사용하는 스탬프는 행사(프로그램) 선택이 필수입니다.
                  </p>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  매칭할 장소
                </label>

                <select
                  value={form.placeId}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      placeId: Number(e.target.value),
                    })
                  }
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value={0}>선택 안 함</option>

                  {places.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* QR 사용 여부 */}
            <div className="flex items-center justify-between border border-gray-200 rounded-lg px-4 py-3">
              <div>
                <p className="text-sm font-medium text-gray-700">
                  QR 스탬프 사용
                </p>

                <p className="text-xs text-gray-500 mt-1">
                  QR을 사용하는 경우 행사(프로그램)와 1:1로 연결됩니다.
                </p>
              </div>

              {editingId ? (
                <div className="flex items-center gap-2 text-sm text-gray-600">
                  <QrCode className="w-4 h-4" />

                  {form.useQrCode
                    ? 'QR 사용 중'
                    : 'QR 미사용'}
                </div>
              ) : (
                <button
                  type="button"
                  onClick={() =>
                    setForm({
                      ...form,
                      useQrCode: !form.useQrCode,
                    })
                  }
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                    form.useQrCode
                      ? 'bg-blue-600'
                      : 'bg-gray-300'
                  }`}
                >
                  <span
                    className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                      form.useQrCode
                        ? 'translate-x-6'
                        : 'translate-x-1'
                    }`}
                  />
                </button>
              )}
            </div>

            {/* QR 안내 */}
            {!editingId && form.useQrCode && (
              <div className="flex items-start gap-3 bg-blue-50 border border-blue-100 rounded-lg px-4 py-3">
                <QrCode className="w-5 h-5 text-blue-600 mt-0.5" />

                <div>
                  <p className="text-sm font-medium text-blue-700">
                    QR 코드가 자동 생성됩니다
                  </p>

                  <p className="text-xs text-blue-600 mt-1">
                    스탬프 등록 후 서버에서 고유한 QR 코드가 생성됩니다.
                  </p>
                </div>
              </div>
            )}

            {/* 운영시간 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                운영시간
              </label>

              <input
                type="text"
                value={form.operatingHours}
                onChange={(e) =>
                  setForm({
                    ...form,
                    operatingHours: e.target.value,
                  })
                }
                placeholder="예: 09:00-18:00"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />

              <p className="text-xs text-gray-500 mt-1">
                QR 스탬프 수집 가능 시간으로 사용됩니다.
              </p>
            </div>

            {/* 발급 조건 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                발급 조건 설정
              </label>

              <select
                value={form.conditionType}
                onChange={(e) =>
                  setForm({
                    ...form,
                    conditionType: e.target.value,
                  })
                }
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                {Object.entries(CONDITION_MAP).map(
                  ([k, v]) => (
                    <option key={k} value={k}>
                      {v}
                    </option>
                  ),
                )}
              </select>
            </div>

            {/* 상세 조건 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                상세 조건
              </label>

              <textarea
                value={form.conditionDetail}
                onChange={(e) =>
                  setForm({
                    ...form,
                    conditionDetail: e.target.value,
                  })
                }
                placeholder="발급 조건에 대한 상세 설명을 입력하세요"
                rows={3}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>

            {/* 이미지 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                스탬프 이미지
              </label>

              <div className="flex items-center gap-4">
                <label className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center hover:border-blue-400 transition-colors cursor-pointer inline-block">
                  <Upload className="w-6 h-6 text-gray-400 mx-auto mb-1" />

                  <p className="text-sm text-gray-600">
                    이미지 업로드
                  </p>

                  <p className="text-xs text-gray-500 mt-0.5">
                    PNG, JPG (최대 5MB)
                  </p>

                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/*"
                    onChange={handleUpload}
                    className="hidden"
                  />
                </label>

                {uploading && (
                  <span className="text-sm text-gray-500">
                    업로드 중...
                  </span>
                )}
              </div>

              {imageUrl && (
                <div className="mt-3 flex items-center gap-3">
                  <img
                    src={imageUrl}
                    alt="미리보기"
                    className="h-20 w-20 rounded-lg object-cover border border-gray-200"
                  />

                  <button
                    onClick={() => {
                      setImageUrl('');

                      if (fileInputRef.current) {
                        fileInputRef.current.value = '';
                      }
                    }}
                    className="text-sm text-red-500 hover:text-red-700"
                  >
                    삭제
                  </button>
                </div>
              )}
            </div>

            {/* 활성 상태 */}
            <div className="flex items-center justify-between border border-gray-200 rounded-lg px-4 py-3">
              <div>
                <p className="text-sm font-medium text-gray-700">
                  스탬프 활성 상태
                </p>

                <p className="text-xs text-gray-500 mt-1">
                  비활성화하면 사용자가 QR로 수집할 수 없습니다.
                </p>
              </div>

              <button
                type="button"
                onClick={() =>
                  setForm({
                    ...form,
                    isActive: !form.isActive,
                  })
                }
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors ${
                  form.isActive
                    ? 'bg-blue-600'
                    : 'bg-gray-300'
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white transition-transform ${
                    form.isActive
                      ? 'translate-x-6'
                      : 'translate-x-1'
                  }`}
                />
              </button>
            </div>

            {/* 저장 */}
            <button
              onClick={handleSubmit}
              disabled={uploading}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
            >
              <Save className="w-4 h-4" />

              {editingId ? '수정' : '등록'}
            </button>
          </div>
        </div>
      )}

      {/* 목록 */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">
            등록된 스탬프
          </h3>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  스탬프명
                </th>

                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  프로그램
                </th>

                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  장소
                </th>

                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  운영시간
                </th>

                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  발급 조건
                </th>

                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  발급 수
                </th>

                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  상태
                </th>

                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  QR
                </th>

                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  관리
                </th>
              </tr>
            </thead>

            <tbody className="divide-y divide-gray-200">
              {stamps.map((stamp) => (
                <tr
                  key={stamp.id}
                  className="hover:bg-gray-50"
                >
                  {/* 스탬프명 */}
                  <td className="px-6 py-4 text-sm text-gray-900">
                    <div className="flex items-center gap-3">
                      {stamp.imageUrl && (
                        <img
                          src={stamp.imageUrl}
                          alt={stamp.name}
                          className="h-8 w-8 rounded object-cover"
                        />
                      )}

                      <div>
                        <div>{stamp.name}</div>

                        {stamp.description && (
                          <div className="text-xs text-gray-400 mt-0.5 max-w-[220px] truncate">
                            {stamp.description}
                          </div>
                        )}
                      </div>
                    </div>
                  </td>

                  {/* 프로그램 */}
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {stamp.program?.name || '-'}
                  </td>

                  {/* 장소 */}
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {stamp.place?.name || '-'}
                  </td>

                  {/* 운영시간 */}
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {stamp.operatingHours || '-'}
                  </td>

                  {/* 조건 */}
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {CONDITION_MAP[stamp.conditionType] ||
                      stamp.conditionType}
                  </td>

                  {/* 발급 수 */}
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {stamp._count.userStamps.toLocaleString()}개
                  </td>

                  {/* 상태 */}
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium ${
                        stamp.isActive
                          ? 'bg-green-100 text-green-700'
                          : 'bg-gray-100 text-gray-500'
                      }`}
                    >
                      {stamp.isActive
                        ? '활성'
                        : '비활성'}
                    </span>
                  </td>

                  {/* QR */}
                  <td className="px-6 py-4">
                    {stamp.qrCode ? (
                      <div
                        className="flex items-center gap-1 text-sm text-gray-600"
                        title={stamp.qrCode}
                      >
                        <QrCode className="w-4 h-4" />

                        <span className="max-w-[120px] truncate">
                          {stamp.qrCode}
                        </span>
                      </div>
                    ) : (
                      <span className="text-xs text-gray-400">
                        없음
                      </span>
                    )}
                  </td>

                  {/* 관리 */}
                  <td className="px-6 py-4">
                    <div className="flex gap-2">
                      <button
                        onClick={() =>
                          openEditForm(stamp)
                        }
                        className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="수정"
                      >
                        <Edit className="w-4 h-4" />
                      </button>

                      <button
                        onClick={() =>
                          handleDelete(stamp.id)
                        }
                        className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                        title="삭제"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}

              {stamps.length === 0 && (
                <tr>
                  <td
                    colSpan={9}
                    className="px-6 py-8 text-center text-sm text-gray-500"
                  >
                    등록된 스탬프가 없습니다
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}