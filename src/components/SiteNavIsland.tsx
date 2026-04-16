"use client";

import { TopBar, Nav, VisdomBrand } from '@virtuslab/visdom-ui';

interface SiteNavIslandProps {
  base: string;
}

const navLinks = (base: string) => [
  { label: 'Home', href: `${base}` },
  { label: 'For Leaders', href: `${base}guide/leaders/` },
  { label: 'For Engineers', href: `${base}guide/platform-engineers/` },
  { label: 'For Developers', href: `${base}guide/developers/` },
  { label: 'Scenarios', href: `${base}before-after/` },
  { label: 'FAQ', href: `${base}faq/` },
  { label: 'Reference', href: `${base}reference/` },
];

export function SiteNavIsland({ base }: SiteNavIslandProps) {
  return (
    <>
      {/* Fixed header: TopBar + Nav stacked */}
      <div className="fixed top-0 left-0 right-0 z-50">
        <TopBar />
        <Nav
          brand={<VisdomBrand product="Testing" href={base} />}
          links={navLinks(base)}
          cta={{ label: 'GitHub', href: 'https://github.com/VirtusLab/visdom-testing' }}
          className="relative top-auto left-auto right-auto"
        />
      </div>
      {/* Spacer to push content below the fixed header (~32px topbar + 64px nav) */}
      <div style={{ height: '96px' }} />
    </>
  );
}
