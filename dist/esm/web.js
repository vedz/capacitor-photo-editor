import { WebPlugin } from '@capacitor/core';
export class PhotoEditorWeb extends WebPlugin {
    async echo(options) {
        console.log('ECHO', options);
        return options;
    }
    editPhoto(options) {
        console.log(options);
        throw new Error("Unimplemented");
    }
}
//# sourceMappingURL=web.js.map