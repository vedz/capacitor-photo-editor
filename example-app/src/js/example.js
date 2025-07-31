import { PhotoEditor } from 'capacitor-photo-editor';
import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';

window.testEcho = () => {
  const inputValue = document.getElementById("echoInput").value;
  PhotoEditor.echo({ value: inputValue })
}

window.editPhoto = async () => {
  const photo = await Camera.getPhoto({
    resultType: CameraResultType.Uri,
    source: CameraSource.Photos
  });
  const result = await PhotoEditor.editPhoto({ image: photo.path });
  console.log('Edited:', result.image);
};

window.editPhotoCAm = async () => {
  const photo = await Camera.getPhoto({
    resultType: CameraResultType.Uri,
    source: CameraSource.Camera
  });
  const result = await PhotoEditor.editPhoto({ image: photo.path });
  console.log('Edited:', result.image);
};
