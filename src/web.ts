import { WebPlugin } from '@capacitor/core';

import type { EditPhotoOptions, EditPhotoResult, PhotoEditorPlugin } from './definitions';

export class PhotoEditorWeb extends WebPlugin implements PhotoEditorPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
  editPhoto(options: EditPhotoOptions): Promise<EditPhotoResult> {
    console.log(options);
    throw new Error("Unimplemented");
  }
}
