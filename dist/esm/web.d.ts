import { WebPlugin } from '@capacitor/core';
import type { EditPhotoOptions, EditPhotoResult, PhotoEditorPlugin } from './definitions';
export declare class PhotoEditorWeb extends WebPlugin implements PhotoEditorPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    editPhoto(options: EditPhotoOptions): Promise<EditPhotoResult>;
}
