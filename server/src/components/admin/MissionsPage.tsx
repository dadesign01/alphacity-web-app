'use client';

import { Plus, Edit, Trash2, X, Save } from 'lucide-react';
import { useEffect, useState } from 'react';

interface Mission {
  id: number;
  name: string;
  type: string;
  place: { id: number; name: string } | null;
  stamp: { id: number; name: string; imageUrl: string | null } | null;
  question: string | null;
  answer: string | null;
  options: string | null;
  stayMinutes: number | null;
  _count: { completions: number };
}

interface Place {
  id: number;
  name: string;
}

interface StampOption {
  id: number;
  name: string;
  imageUrl: string | null;
}

const TYPE_MAP: Record<string, string> = { quiz: '퀴즈', location_auth: '위치 인증', stay_time: '체류시간' };

const TYPE_TO_FORM: Record<string, string> = { quiz: 'quiz', location_auth: 'location', stay_time: 'stay' };
const FORM_TO_TYPE: Record<string, string> = { quiz: 'quiz', location: 'location_auth', stay: 'stay_time' };

const EMPTY_FORM = { name: '', placeId: 0, stampId: 0, question: '', answer: '', options: ['', '', '', ''], stayMinutes: 5 };

export default function MissionsPage() {
  const [missions, setMissions] = useState<Mission[]>([]);
  const [places, setPlaces] = useState<Place[]>([]);
  const [stamps, setStamps] = useState<StampOption[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [missionType, setMissionType] = useState('quiz');
  const [quizFormat, setQuizFormat] = useState<'multiple_choice' | 'short_answer'>('multiple_choice');
  const [form, setForm] = useState(EMPTY_FORM);

  const fetchMissions = () => {
    fetch('/api/v1/admin/missions').then((r) => r.json()).then((d) => { if (d.success) setMissions(d.data); });
  };

  useEffect(() => {
    fetchMissions();
    fetch('/api/v1/admin/places').then((r) => r.json()).then((d) => {
      if (d.success) setPlaces(d.data.map((p: Place) => ({ id: p.id, name: p.name })));
    });
    fetch('/api/v1/admin/stamps').then((r) => r.json()).then((d) => {
      if (d.success) setStamps(d.data.map((s: StampOption) => ({ id: s.id, name: s.name, imageUrl: s.imageUrl })));
    });
  }, []);

  const openCreateForm = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setMissionType('quiz');
    setQuizFormat('multiple_choice');
    setShowForm(true);
  };

  const openEditForm = (mission: Mission) => {
    setEditingId(mission.id);
    setMissionType(TYPE_TO_FORM[mission.type] || 'quiz');
    let parsedOptions = ['', '', '', ''];
    if (mission.options) {
      try { parsedOptions = JSON.parse(mission.options); } catch { /* ignore */ }
    }
    // 보기가 없으면 서술형, 있으면 객관식
    if (mission.type === 'quiz') {
      const hasOptions = mission.options && JSON.parse(mission.options).length > 0;
      setQuizFormat(hasOptions ? 'multiple_choice' : 'short_answer');
    }
    setForm({
      name: mission.name,
      placeId: mission.place?.id || 0,
      stampId: mission.stamp?.id || 0,
      question: mission.question || '',
      answer: mission.answer || '',
      options: parsedOptions,
      stayMinutes: mission.stayMinutes || 5,
    });
    setShowForm(true);
  };

  const handleSubmit = async () => {
    const isEdit = editingId !== null;
    const url = isEdit ? `/api/v1/admin/missions/${editingId}` : '/api/v1/admin/missions';
    const method = isEdit ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          name: form.name,
          type: FORM_TO_TYPE[missionType],
          placeId: form.placeId || null,
          stampId: form.stampId || null,
          question: missionType === 'quiz' ? form.question : null,
          answer: missionType === 'quiz' ? form.answer : null,
          options: missionType === 'quiz' && quizFormat === 'multiple_choice' ? JSON.stringify(form.options.filter(o => o.trim())) : null,
          stayMinutes: missionType === 'stay' ? form.stayMinutes : null,
        }),
      });
      const data = await res.json();
      if (data.success) {
        setShowForm(false);
        setEditingId(null);
        setForm(EMPTY_FORM);
        fetchMissions();
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
      const res = await fetch(`/api/v1/admin/missions/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        fetchMissions();
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
          <h2 className="text-2xl font-semibold text-gray-900">미션 관리</h2>
          <p className="text-sm text-gray-500 mt-1">사용자 미션을 생성하고 관리합니다</p>
        </div>
        <button onClick={openCreateForm}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          미션 등록
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              {editingId ? '미션 수정' : '새 미션 등록'}
            </h3>
            <button onClick={() => { setShowForm(false); setEditingId(null); }} className="text-gray-400 hover:text-gray-600">
              <X className="w-5 h-5" />
            </button>
          </div>
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
              <label className="block text-sm font-medium text-gray-700 mb-2">완료 시 적립 스탬프</label>
              <select value={form.stampId} onChange={(e) => setForm({ ...form, stampId: Number(e.target.value) })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                <option value={0}>스탬프 없음</option>
                {stamps.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              </select>
              <p className="text-xs text-gray-500 mt-1">미션 완료 시 자동으로 이 스탬프가 적립됩니다</p>
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
                  <label className="block text-sm font-medium text-gray-700 mb-2">퀴즈 형식</label>
                  <div className="flex gap-3">
                    {[{ value: 'multiple_choice' as const, label: '객관식' }, { value: 'short_answer' as const, label: '서술형' }].map((fmt) => (
                      <button key={fmt.value} onClick={() => setQuizFormat(fmt.value)}
                        className={`px-4 py-2 text-sm rounded-lg transition-colors ${
                          quizFormat === fmt.value ? 'bg-purple-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}>
                        {fmt.label}
                      </button>
                    ))}
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">퀴즈 질문</label>
                  <input type="text" value={form.question} onChange={(e) => setForm({ ...form, question: e.target.value })}
                    placeholder="퀴즈 질문을 입력하세요"
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                </div>
                {quizFormat === 'multiple_choice' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">보기 (객관식)</label>
                    <div className="space-y-2">
                      {form.options.map((opt, idx) => (
                        <div key={idx} className="flex items-center gap-2">
                          <span className="text-sm text-gray-500 w-6">{idx + 1}.</span>
                          <input type="text" value={opt}
                            onChange={(e) => {
                              const newOptions = [...form.options];
                              newOptions[idx] = e.target.value;
                              setForm({ ...form, options: newOptions });
                            }}
                            placeholder={`보기 ${idx + 1}`}
                            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
                        </div>
                      ))}
                      {form.options.length < 6 && (
                        <button type="button" onClick={() => setForm({ ...form, options: [...form.options, ''] })}
                          className="text-sm text-blue-600 hover:text-blue-700">+ 보기 추가</button>
                      )}
                    </div>
                    <p className="text-xs text-gray-500 mt-1">정답도 보기에 포함되어야 합니다</p>
                  </div>
                )}
                {quizFormat === 'short_answer' && (
                  <p className="text-xs text-gray-500">서술형은 사용자가 직접 답을 입력합니다. 대소문자 구분 없이 정답과 일치하면 정답 처리됩니다.</p>
                )}
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

            <button onClick={handleSubmit}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
              <Save className="w-4 h-4" />
              {editingId ? '수정' : '등록'}
            </button>
          </div>
        </div>
      )}

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">등록된 미션</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">미션명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">유형</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">적립 스탬프</th>
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
                  {mission.stamp ? (
                    <span className="inline-flex items-center gap-1 px-2 py-1 text-xs rounded-full bg-yellow-100 text-yellow-800">
                      🏅 {mission.stamp.name}
                    </span>
                  ) : <span className="text-gray-400">-</span>}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {mission.question || (mission.place?.name ? `${mission.place.name}${mission.stayMinutes ? ` / ${mission.stayMinutes}분` : ''}` : '-')}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">{mission._count.completions}건</td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    <button onClick={() => openEditForm(mission)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                      <Edit className="w-4 h-4" />
                    </button>
                    <button onClick={() => handleDelete(mission.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {missions.length === 0 && (
              <tr><td colSpan={6} className="px-6 py-8 text-center text-sm text-gray-500">등록된 미션이 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
