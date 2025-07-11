import { PhotoEditor } from 'capacitor-photo-editor';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    PhotoEditor.echo({ value: inputValue })
}
