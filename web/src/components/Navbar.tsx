import { useState } from 'react';
import { FiMenu, FiX, FiGithub, FiDownload } from 'react-icons/fi';

export default function Navbar() {
  const [open, setOpen] = useState(false);

  const links = [
    { label: 'Features', href: '#features' },
    { label: 'Screenshots', href: '#screenshots' },
    { label: 'Tech Stack', href: '#tech' },
    { label: 'Changelog', href: '#changelog' },
  ];

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-axiom-black/90 backdrop-blur-sm border-b border-axiom-border">
      <div className="max-w-6xl mx-auto px-6 h-16 flex items-center justify-between">
        <a href="#" className="flex items-center gap-3 group">
          <img src="./axiom-logo.png" alt="axiom." className="w-8 h-8 object-contain" />
          <span className="font-space text-sm tracking-[0.2em] text-axiom-white group-hover:text-axiom-red transition-colors">
            a<span className="text-axiom-red">x</span>i<span className="text-axiom-red">o</span>m.
          </span>
        </a>

        <div className="hidden md:flex items-center gap-8">
          {links.map((link) => (
            <a
              key={link.label}
              href={link.href}
              className="font-ndot text-xs tracking-widest uppercase text-axiom-gray-muted hover:text-axiom-red transition-colors"
            >
              {link.label}
            </a>
          ))}
          <a
            href="https://github.com/RA-L-PH/axiom"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 font-ndot text-xs tracking-widest uppercase text-axiom-gray-muted hover:text-axiom-white transition-colors"
          >
            <FiGithub size={14} />
            GitHub
          </a>
          <a
            href="https://github.com/RA-L-PH/axiom/releases/latest"
            target="_blank"
            rel="noopener noreferrer"
            className="flex items-center gap-2 px-5 py-2 bg-axiom-red text-axiom-white font-ndot text-xs tracking-widest uppercase hover:bg-axiom-red-dark transition-colors"
          >
            <FiDownload size={14} />
            Download
          </a>
        </div>

        <button
          onClick={() => setOpen(!open)}
          className="md:hidden text-axiom-white p-2 border border-axiom-border hover:border-axiom-red transition-colors"
          aria-label="Toggle menu"
        >
          {open ? <FiX size={20} /> : <FiMenu size={20} />}
        </button>
      </div>

      {open && (
        <div className="md:hidden border-t border-axiom-border bg-axiom-black">
          {links.map((link) => (
            <a
              key={link.label}
              href={link.href}
              onClick={() => setOpen(false)}
              className="block px-6 py-4 font-ndot text-xs tracking-widest uppercase text-axiom-gray-muted hover:text-axiom-red hover:bg-axiom-gray transition-colors border-b border-axiom-border"
            >
              {link.label}
            </a>
          ))}
          <div className="flex gap-0">
            <a
              href="https://github.com/RA-L-PH/axiom"
              target="_blank"
              rel="noopener noreferrer"
              className="flex-1 flex items-center justify-center gap-2 py-4 font-ndot text-xs tracking-widest uppercase text-axiom-gray-muted hover:text-axiom-white border-b border-axiom-border border-r border-axiom-border"
            >
              <FiGithub size={14} />
              GitHub
            </a>
            <a
              href="https://github.com/RA-L-PH/axiom/releases/latest"
              target="_blank"
              rel="noopener noreferrer"
              className="flex-1 flex items-center justify-center gap-2 py-4 font-ndot text-xs tracking-widest uppercase text-axiom-white bg-axiom-red hover:bg-axiom-red-dark"
            >
              <FiDownload size={14} />
              Download
            </a>
          </div>
        </div>
      )}
    </nav>
  );
}
