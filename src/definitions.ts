export interface EditPhotoOptions {
  image: string; // file:// or content:// URI
}

export interface EditPhotoResult {
  image: string;
}

export interface PhotoEditorPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  editPhoto(options: EditPhotoOptions): Promise<EditPhotoResult>
}
