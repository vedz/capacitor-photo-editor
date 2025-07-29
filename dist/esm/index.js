import { registerPlugin } from '@capacitor/core';
const PhotoEditor = registerPlugin('PhotoEditor', {
    web: () => import('./web').then((m) => new m.PhotoEditorWeb()),
});
export * from './definitions';
export { PhotoEditor };
//# sourceMappingURL=index.js.map