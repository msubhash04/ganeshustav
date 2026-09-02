import React from 'react'
import PublicPageHeader from '../components/common/PublicPageHeader'
import { Mail, MapPin, Search, LogIn, KeyRound } from 'lucide-react'
import Footer from '../components/common/Footer'

const TOPICS = [
  {
    icon: Search,
    title: "Looking up a committee's public page",
    desc: 'Use the "Access Public Features" search on the homepage with the committee\'s Ganesh Unique Code — no login needed.',
  },
  {
    icon: LogIn,
    title: "Can't log in?",
    desc: 'Double-check your username and password with your committee\'s President. Only the President can add or reset staff accounts.',
  },
  {
    icon: KeyRound,
    title: 'Forgot your password?',
    desc: 'There is no self-service reset yet — ask your committee\'s President to set a new password for your account from the Members page.',
  },
]

export default function Support() {
  return (
    <div className="min-h-screen flex flex-col bg-cream">
      <PublicPageHeader />

      <main className="flex-1 max-w-2xl mx-auto px-4 py-8 w-full">
        <h1 className="page-title mb-1">Support</h1>
        <p className="text-sm text-maroon-500 mb-6">Common questions, and how to reach us for anything else.</p>

        <div className="space-y-3 mb-6">
          {TOPICS.map(({ icon: Icon, title, desc }) => (
            <div key={title} className="card flex gap-4">
              <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-saffron-500 to-maroon-600 flex items-center justify-center text-white shrink-0">
                <Icon size={18} />
              </div>
              <div>
                <h3 className="font-semibold text-maroon-800 mb-0.5">{title}</h3>
                <p className="text-sm text-maroon-500">{desc}</p>
              </div>
            </div>
          ))}
        </div>

        <div className="card bg-gradient-to-r from-saffron-500 to-maroon-600 text-white space-y-4">
          <div className="flex items-center gap-3">
            <Mail size={20} className="shrink-0" />
            <div>
              <p className="font-semibold">Still need help?</p>
              <a href="mailto:help@lenvytechnologies.in" className="text-saffron-100 underline underline-offset-2">
                help@lenvytechnologies.in
              </a>
            </div>
          </div>
          <div className="flex items-start gap-3 pt-4 border-t border-white/20">
            <MapPin size={20} className="shrink-0 mt-0.5" />
            <p className="text-sm text-saffron-50 leading-relaxed">
              7th Floor, Summit B, Brigade Metropolis, Whitefield Main Rd, Mahadevapura, Bengaluru, Karnataka 560048, India
            </p>
          </div>
        </div>
      </main>

      <Footer />
    </div>
  )
}
