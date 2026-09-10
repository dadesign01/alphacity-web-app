import { BrowserQRCodeReader } from "@zxing/browser";

export default async function startQrScanner(video, callback) {
    const stream = await navigator.mediaDevices.getUserMedia({
        video: {
            facingMode: {
                ideal: "environment"
            }
        },
        audio: false
    });

    video.srcObject = stream;
    video.setAttribute("playsinline", "true");
    video.autoplay = true;
    video.muted = true;

    await video.play();

    const reader = new BrowserQRCodeReader();

    await reader.decodeFromStream(
        stream,
        video,
        function (result, error, controls) {
            if (result) {
                const text = result.getText();

                console.log("=== QR CODE DETECTED ===");
                console.log(text);

                callback(text);
            }
        }
    );

    return {
        stream: stream
    };
}