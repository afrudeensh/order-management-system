export function fileToResizedDataUrl(
  file: File,
  maxSize = 400,
  quality = 0.8,
): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();

    reader.onerror = () => reject(new Error('Could not read the file'));
    reader.onload = () => {
      const img = new Image();

      img.onerror = () => reject(new Error('This file is not a valid image'));
      img.onload = () => {
        const scale = Math.min(1, maxSize / Math.max(img.width, img.height));
        const w = Math.round(img.width * scale);
        const h = Math.round(img.height * scale);

        const canvas = document.createElement('canvas');
        canvas.width = w;
        canvas.height = h;

        const ctx = canvas.getContext('2d')!;
        ctx.fillStyle = '#fff';            // transparent PNGs get a white background
        ctx.fillRect(0, 0, w, h);
        ctx.drawImage(img, 0, 0, w, h);

        resolve(canvas.toDataURL('image/jpeg', quality));
      };

      img.src = reader.result as string;
    };

    reader.readAsDataURL(file);
  });
}