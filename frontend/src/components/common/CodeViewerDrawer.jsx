import React, { useState, useEffect } from 'react'
import { X, Code2, ShieldCheck, FileBarChart, Search } from 'lucide-react'

const SNIPPETS = {
  yearGuard: {
    label: 'Year Creation Guard',
    icon: ShieldCheck,
    file: 'FestivalYearService.java',
    code: `int currentYear = LocalDate.now().getYear();
if (dto.getYear() == null || dto.getYear() != currentYear) {
    throw new IllegalStateException(
        "A festival can only be created for the current "
      + "calendar year (" + currentYear + "). "
      + (dto.getYear() < currentYear
          ? "Creating festivals for past years is not allowed."
          : "Future festivals unlock automatically once that "
          + "year begins."));
}

// RULE: only one festival per calendar year, per committee
if (festivalYearRepository.existsByCommitteeIdAndYear(
        committee.getId(), dto.getYear())) {
    throw new IllegalStateException(
        "A festival for " + dto.getYear() + " has already "
      + "been created.");
}

// Automatically archives every earlier year for this
// committee the moment a new one is created.
festivalYearRepository.deactivateAllForCommittee(committee.getId());`,
    blurb: 'One check enforces past-year, future-year, and one-per-year rules together — a year is only valid if it equals the current calendar year, full stop.',
  },
  rbac: {
    label: 'RBAC Middleware',
    icon: ShieldCheck,
    file: 'JwtAuthFilter.java + MemberController.java',
    code: `// JwtAuthFilter - authorities are built from the JWT's own
// signed claims, never trusted from the request itself
String role = jwtUtil.extractRole(token);
authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

// Tenant Inspection's ADMIN_OVERRIDE mode additionally grants
// ROLE_PRESIDENT for the session, but the real role claim
// (DEVELOPER) is never overwritten - audit trails stay truthful
if (mode == InspectionMode.ADMIN_OVERRIDE) {
    authorities.add(new SimpleGrantedAuthority("ROLE_PRESIDENT"));
}

// MemberController - split per method, not at the class level
@GetMapping
@PreAuthorize("hasAnyRole('PRESIDENT','DEVELOPER')")
public List<MemberResponse> getAll() { ... }

@PostMapping
@PreAuthorize("hasRole('PRESIDENT')")
public MemberResponse create(...) { ... }`,
    blurb: "Every request's role comes from a signed JWT claim, and every endpoint states exactly which roles may call it — viewing and mutating are authorized separately.",
  },
  auditReport: {
    label: 'Audit Report Generator',
    icon: FileBarChart,
    file: 'ReportService.java',
    code: `BigDecimal totalCollections = donationRepository
    .getTotalCollectionByFestivalYear(yearId);
BigDecimal totalExpenses = expenseRepository
    .getTotalExpensesByFestivalYear(yearId);
BigDecimal totalAuctionEarnings = auctionItemRepository
    .getTotalAuctionAmount(yearId);
BigDecimal totalSponsorships = generalSponsorshipTotal
    .add(annadanamSponsorshipTotal);

// carryForward + collections + sponsorships + auction - expenses
BigDecimal netSurplusOrDeficit = carryForward
    .add(totalCollections)
    .add(totalSponsorships)
    .add(totalAuctionEarnings)
    .subtract(totalExpenses);`,
    blurb: 'Every festival year — active or archived — is reduced to the same five numbers, so this year and any past year always compare apples to apples.',
  },
  codeLookup: {
    label: 'Committee Code Lookup',
    icon: Search,
    file: 'PublicService.java',
    code: `private Committee findCommittee(String tenantCode) {
    return committeeRepository.findByTenantCode(tenantCode)
        .orElseThrow(() -> new EntityNotFoundException(
            "No committee found with code: " + tenantCode));
}

// found=false (not a 404) means the code is valid but nothing
// is live right now - lets the UI tell "Invalid Code" apart
// from "Festival Not Found / no live festival at the moment"
if (active == null) {
    return Response.builder()
        .committeeName(committee.getName())
        .tenantCode(committee.getTenantCode())
        .found(false)
        .build();
}`,
    blurb: 'The same Ganesh Unique Code shown on every receipt and transparency page is the only key needed to look up a committee — no login, no session.',
  },
}

export const DEMO_CHIP_KEYS = Object.keys(SNIPPETS).map((key) => ({ key, label: SNIPPETS[key].label }))

export default function CodeViewerDrawer({ open, onClose, initialKey = 'yearGuard' }) {
  const [activeKey, setActiveKey] = useState(initialKey)

  // Jump straight to the snippet whose chip was clicked, each time the
  // drawer is (re)opened - without this, every chip on the landing page
  // would open to the same default tab regardless of which was tapped.
  useEffect(() => {
    if (open) setActiveKey(initialKey)
  }, [open, initialKey])

  const active = SNIPPETS[activeKey]

  if (!open) return null

  return (
    <div className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-maroon-900/50 backdrop-blur-sm p-0 sm:p-4">
      <div className="bg-white w-full sm:max-w-2xl sm:rounded-2xl rounded-t-2xl shadow-xl max-h-[85vh] flex flex-col">
        <div className="flex items-center justify-between px-5 py-4 border-b border-saffron-100">
          <h3 className="font-display font-bold text-maroon-800 flex items-center gap-2">
            <Code2 size={20} className="text-saffron-500" /> Under the Hood
          </h3>
          <button onClick={onClose} className="text-maroon-400 hover:text-maroon-700 transition">
            <X size={20} />
          </button>
        </div>

        <div className="flex gap-2 px-5 pt-4 flex-wrap">
          {Object.entries(SNIPPETS).map(([key, s]) => (
            <button
              key={key}
              onClick={() => setActiveKey(key)}
              className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium border transition ${
                activeKey === key
                  ? 'bg-saffron-500 text-white border-saffron-500'
                  : 'bg-white text-maroon-500 border-saffron-200 hover:bg-saffron-50'
              }`}
            >
              <s.icon size={13} /> {s.label}
            </button>
          ))}
        </div>

        <div className="px-5 py-4 overflow-y-auto">
          <p className="text-sm text-maroon-500 mb-3">{active.blurb}</p>
          <p className="text-xs text-maroon-400 font-mono mb-2">{active.file}</p>
          <pre className="bg-maroon-900 text-saffron-100 text-xs leading-relaxed rounded-xl p-4 overflow-x-auto">
            <code>{active.code}</code>
          </pre>
        </div>
      </div>
    </div>
  )
}
