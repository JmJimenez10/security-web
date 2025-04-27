/// <reference types="vite/client" />

// CSS modules
declare module "*.module.css" {
  const classes: { [key: string]: string };
  export default classes;
}
declare module "*.module.scss" {
  const classes: { [key: string]: string };
  export default classes;
}
declare module "*.module.sass" {
  const classes: { [key: string]: string };
  export default classes;
}

// Normal CSS / SCSS imports
declare module "*.css";
declare module "*.scss";
declare module "*.sass";

// Images
declare module "*.png" {
  const src: string;
  export default src;
}
declare module "*.jpg" {
  const src: string;
  export default src;
}
declare module "*.jpeg" {
  const src: string;
  export default src;
}
declare module "*.gif" {
  const src: string;
  export default src;
}
declare module "*.webp" {
  const src: string;
  export default src;
}
declare module "*.avif" {
  const src: string;
  export default src;
}

// SVGs: importar como componente de React o como URL
declare module "*.svg" {
  import * as React from "react";
  export const ReactComponent: React.FunctionComponent<React.SVGProps<SVGSVGElement> & { title?: string }>;
  const src: string;
  export default src;
}

// Fonts
declare module "*.woff";
declare module "*.woff2";
declare module "*.eot";
declare module "*.ttf";
declare module "*.otf";

// Media
declare module "*.mp4" {
  const src: string;
  export default src;
}
declare module "*.webm" {
  const src: string;
  export default src;
}
declare module "*.ogg" {
  const src: string;
  export default src;
}
declare module "*.mp3" {
  const src: string;
  export default src;
}
declare module "*.wav" {
  const src: string;
  export default src;
}
declare module "*.flac" {
  const src: string;
  export default src;
}
declare module "*.aac" {
  const src: string;
  export default src;
}

// JSON files (opcional porque TS ya soporta, pero por si acaso)
declare module "*.json" {
  const value: unknown;
  export default value;
}
