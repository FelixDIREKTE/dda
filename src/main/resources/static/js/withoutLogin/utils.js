if (typeof DDA === 'undefined') {
    var DDA = {};
}
//TODO unnötig, löschen?
DDA.Utils = new function () {
    return {

        b64ToBlob: function (b64Str, contentType = '', sliceSize = 512) {
            const byteCharacters = atob(b64Str);
            const byteArrays = [];

            for (let offset = 0; offset < byteCharacters.length; offset += sliceSize) {
                const slice = byteCharacters.slice(offset, offset + sliceSize);

                const byteNumbers = new Array(slice.length);
                for (let i = 0; i < slice.length; i++) {
                    byteNumbers[i] = slice.charCodeAt(i);
                }

                const byteArray = new Uint8Array(byteNumbers);
                byteArrays.push(byteArray);
            }

            const blob = new Blob(byteArrays, {type: contentType});
            return blob;
        },

        parseDate(input) {
        let parts = input.match(/(\d+)/g);
        return new Date(parts[2], parts[1]-1, parts[0]);
    }

    }
};