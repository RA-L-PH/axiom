import { motion } from 'framer-motion';
import { FiGithub, FiSmartphone, FiDownload } from 'react-icons/fi';

const variants = [
  {
    arch: 'Universal',
    desc: 'Works on all devices',
    url: 'https://github.com/RA-L-PH/axiom/releases/download/v0.1.2-beta.2/axiom-0.1.2-beta.2-normal-universal.apk',
    primary: true,
  },
  {
    arch: 'arm64-v8a',
    desc: 'Most modern phones',
    url: 'https://github.com/RA-L-PH/axiom/releases/download/v0.1.2-beta.2/axiom-0.1.2-beta.2-normal-arm64-v8a.apk',
    primary: false,
  },
  {
    arch: 'armeabi-v7a',
    desc: 'Older ARM devices',
    url: 'https://github.com/RA-L-PH/axiom/releases/download/v0.1.2-beta.2/axiom-0.1.2-beta.2-normal-armeabi-v7a.apk',
    primary: false,
  },
  {
    arch: 'x86_64',
    desc: '64-bit emulators',
    url: 'https://github.com/RA-L-PH/axiom/releases/download/v0.1.2-beta.2/axiom-0.1.2-beta.2-normal-x86_64.apk',
    primary: false,
  },
  {
    arch: 'x86',
    desc: '32-bit emulators',
    url: 'https://github.com/RA-L-PH/axiom/releases/download/v0.1.2-beta.2/axiom-0.1.2-beta.2-normal-x86.apk',
    primary: false,
  },
];

export default function DownloadSection() {
  return (
    <section id="download" className="py-24 px-6">
      <div className="max-w-6xl mx-auto">
        <div className="mb-16 flex items-center gap-4">
          <div className="h-px flex-1 bg-axiom-border" />
          <h2 className="font-ndot text-xs tracking-[0.3em] uppercase text-axiom-red">
            Download
          </h2>
          <div className="h-px flex-1 bg-axiom-border" />
        </div>

        <motion.div
          initial={{ opacity: 0, y: 30 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          className="text-center"
        >
          <img src="/axiom-logo.png" alt="axiom." className="w-20 h-20 object-contain mx-auto mb-8" />

          <h3 className="text-3xl md:text-4xl font-light text-axiom-white mb-4 font-space">
            Get a<span className="text-axiom-red">x</span>i<span className="text-axiom-red">o</span>m<span className="text-axiom-red">.</span>
          </h3>
          <p className="text-axiom-gray-muted max-w-md mx-auto mb-12 font-space">
            Free. Open source. No ads. No tracking. Just music.
          </p>

          <div className="max-w-lg mx-auto mb-6">
            <a
              href={variants[0].url}
              target="_blank"
              rel="noopener noreferrer"
              className="group flex items-center justify-center gap-4 p-8 bg-axiom-red hover:bg-axiom-red-dark text-axiom-white transition-colors"
            >
              <FiDownload size={24} />
              <div className="text-left">
                <div className="font-ndot text-sm tracking-wider uppercase">
                  Download Universal APK
                </div>
                <div className="mt-1 font-ntype-mono text-[10px] tracking-wider uppercase text-axiom-white/70">
                  v0.1.2-beta.2 — Works on all Android devices
                </div>
              </div>
            </a>
          </div>

          <div className="max-w-2xl mx-auto grid grid-cols-2 sm:grid-cols-4 gap-px bg-axiom-border">
            {variants.slice(1).map((v) => (
              <a
                key={v.arch}
                href={v.url}
                target="_blank"
                rel="noopener noreferrer"
                className="group bg-axiom-black p-5 flex flex-col items-center gap-2 hover:bg-axiom-gray transition-colors"
              >
                <FiGithub size={16} className="text-axiom-gray-muted group-hover:text-axiom-red transition-colors" />
                <span className="font-ndot text-xs tracking-wider text-axiom-white">
                  {v.arch}
                </span>
                <span className="font-ntype-mono text-[9px] tracking-wider uppercase text-axiom-gray-muted">
                  {v.desc}
                </span>
              </a>
            ))}
          </div>

          <div className="mt-12 flex items-center justify-center gap-6 font-ntype-mono text-[10px] tracking-[0.2em] uppercase text-axiom-gray-muted">
            <div className="flex items-center gap-2">
              <FiSmartphone size={12} />
              <span>Android 8.0+</span>
            </div>
            <div className="w-px h-3 bg-axiom-border" />
            <span>v0.1.2-beta.2</span>
            <div className="w-px h-3 bg-axiom-border" />
            <span>GPL-3.0</span>
          </div>
        </motion.div>
      </div>
    </section>
  );
}
