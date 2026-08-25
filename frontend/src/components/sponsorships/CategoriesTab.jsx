import React, { useEffect, useState } from 'react'
import { Plus, Pencil, Trash2, Tag } from 'lucide-react'
import Modal from '../common/Modal'
import { sponsorshipCategoryApi } from '../../api/sponsorshipApi'

const emptyForm = { name: '', description: '', active: true }

export default function CategoriesTab() {
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)

  const load = () => {
    setLoading(true)
    sponsorshipCategoryApi.getAll().then(setCategories).finally(() => setLoading(false))
  }

  useEffect(load, [])

  const openAdd = () => { setEditing(null); setForm(emptyForm); setFormOpen(true) }
  const openEdit = (c) => { setEditing(c); setForm({ name: c.name, description: c.description || '', active: c.active }); setFormOpen(true) }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      if (editing) {
        await sponsorshipCategoryApi.update(editing.id, form)
      } else {
        await sponsorshipCategoryApi.create(form)
      }
      setFormOpen(false)
      load()
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to save category')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (c) => {
    if (!confirm(`Delete category "${c.name}"?`)) return
    try {
      await sponsorshipCategoryApi.remove(c.id)
      load()
    } catch (err) {
      alert(err?.response?.data?.error || 'Could not delete this category — it may already have sponsors assigned to it. Try marking it inactive instead.')
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <p className="text-sm text-maroon-500">Define sponsorship categories (e.g. Vigraha Dhata, Laddu Dhata) that appear in the General Sponsors dropdown.</p>
        <button onClick={openAdd} className="btn-primary inline-flex items-center gap-2 text-sm py-2 px-3 shrink-0 ml-3">
          <Plus size={16} /> Add Category
        </button>
      </div>

      {loading ? (
        <p className="text-maroon-400">Loading…</p>
      ) : categories.length === 0 ? (
        <div className="text-center py-10">
          <Tag className="mx-auto text-saffron-400 mb-2" size={28} />
          <p className="text-maroon-500 text-sm">No sponsorship categories yet.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-maroon-500 border-b border-saffron-100">
                <th className="py-2 pr-4">Name</th>
                <th className="py-2 pr-4">Description</th>
                <th className="py-2 pr-4">Status</th>
                <th className="py-2 pr-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((c) => (
                <tr key={c.id} className="border-b border-saffron-50 last:border-0">
                  <td className="py-2.5 pr-4 font-medium text-maroon-800">{c.name}</td>
                  <td className="py-2.5 pr-4 text-maroon-500">{c.description || '—'}</td>
                  <td className="py-2.5 pr-4">
                    <span className={`badge ${c.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                      {c.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="py-2.5 pr-4">
                    <div className="flex justify-end gap-2">
                      <button onClick={() => openEdit(c)} className="text-maroon-400 hover:text-saffron-600">
                        <Pencil size={16} />
                      </button>
                      <button onClick={() => handleDelete(c)} className="text-maroon-400 hover:text-maroon-700">
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editing ? 'Edit Category' : 'Add Sponsorship Category'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label-text">Category Name *</label>
            <input className="input-field" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                   placeholder="e.g. Vigraha Dhata (Idol Sponsor)" required />
          </div>
          <div>
            <label className="label-text">Description (optional)</label>
            <textarea className="input-field" rows={2} value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>
          {editing && (
            <label className="flex items-center gap-2 text-sm text-maroon-700">
              <input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
              Active (visible in the General Sponsors dropdown)
            </label>
          )}
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Saving…' : editing ? 'Update Category' : 'Add Category'}
            </button>
            <button type="button" className="btn-secondary" onClick={() => setFormOpen(false)}>Cancel</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
