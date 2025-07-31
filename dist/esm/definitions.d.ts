export interface EditPhotoOptions {
    image: string;
}
export interface EditPhotoResult {
    image: string;
}
export interface PhotoEditorPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    editPhoto(options: EditPhotoOptions): Promise<EditPhotoResult>;
}
