
const links = [
  {
    group: 'Project',
    items: [
      { label: 'Source Code', href: 'https://github.com/RA-L-PH/axiom' },
      { label: 'Releases', href: 'https://github.com/RA-L-PH/axiom/releases' },
    ],
  },
  {
    group: 'Developer',
    items: [
      { label: 'GitHub', href: 'https://github.com/RA-L-PH' },
      { label: 'Portfolio', href: 'https://ra-l-ph.pages.dev' },
    ],
  },
  {
    group: 'Community',
    items: [
      { label: 'Contributing', href: 'https://github.com/RA-L-PH/axiom/blob/master/CONTRIBUTING.md' },
    ],
  },
  {
    group: 'Credits',
    items: [
      { label: 'Retro Music Player', href: 'https://github.com/RetroMusicPlayer/RetroMusicPlayer' },
      { label: 'License (GPL-3.0)', href: 'https://github.com/RA-L-PH/axiom/blob/master/LICENSE.txt' },
    ],
  },
];

export default function Footer() {
  return (
    <footer className="border-t border-axiom-border">
      <div className="max-w-6xl mx-auto px-6">
        <div className="py-16 grid grid-cols-2 md:grid-cols-5 gap-12">
          <div className="col-span-2 md:col-span-1">
            <div className="flex items-center gap-3 mb-4">
              <img src="./axiom-logo.png" alt="axiom." className="w-7 h-7 object-contain" />
              <span className="font-ndot text-sm tracking-[0.2em] text-axiom-white">
                a<span className="text-axiom-red">x</span>i<span className="text-axiom-red">o</span>m<span className="text-axiom-red">.</span>
              </span>
            </div>
            <p className="text-sm text-axiom-gray-muted leading-relaxed font-space">
              Modern design. Pure sound. Fully yours.
            </p>
          </div>

          {links.map((group) => (
            <div key={group.group}>
              <h4 className="font-ndot text-[10px] tracking-[0.2em] uppercase text-axiom-red mb-4">
                {group.group}
              </h4>
              <ul className="space-y-2">
                {group.items.map((item) => (
                  <li key={item.label}>
                    <a
                      href={item.href}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="font-ntype-mono text-xs text-axiom-gray-muted hover:text-axiom-white transition-colors"
                    >
                      {item.label}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="py-6 border-t border-axiom-border flex flex-col sm:flex-row items-center justify-center gap-4">
          <p className="font-ntype-mono text-[10px] tracking-[0.15em] text-axiom-gray-muted text-center">
            &copy; 2026 RA-L-PH. Licensed under GPL-3.0.
          </p>
        </div>
      </div>
    </footer>
  );
}
