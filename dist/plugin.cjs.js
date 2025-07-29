'use strict';

var core = require('@capacitor/core');

const PhotoEditor = core.registerPlugin('PhotoEditor', {
    web: () => Promise.resolve().then(function () { return web; }).then((m) => new m.PhotoEditorWeb()),
});

class PhotoEditorWeb extends core.WebPlugin {
    async echo(options) {
        console.log('ECHO', options);
        return options;
    }
    editPhoto(options) {
        console.log(options);
        throw new Error("Unimplemented");
    }
}

var web = /*#__PURE__*/Object.freeze({
    __proto__: null,
    PhotoEditorWeb: PhotoEditorWeb
});

exports.PhotoEditor = PhotoEditor;
//# sourceMappingURL=plugin.cjs.js.map
