function parseFilename(contentDisposition, fallbackFilename) {
  if (!contentDisposition) {
    return fallbackFilename
  }
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }
  const plainMatch = contentDisposition.match(/filename="?([^"]+)"?/i)
  return plainMatch?.[1] || fallbackFilename
}

async function resolveBlobError(blob) {
  const text = await blob.text()
  if (!text) {
    throw new Error('下载失败')
  }
  try {
    const payload = JSON.parse(text)
    throw new Error(payload.message || '下载失败')
  } catch (error) {
    if (error instanceof SyntaxError) {
      throw new Error(text)
    }
    throw error
  }
}

async function normalizeBlobResponse(response, fallbackFilename) {
  const blob = response.data instanceof Blob ? response.data : new Blob([response.data])
  const contentType = blob.type || response.headers?.['content-type'] || ''
  if (contentType.includes('application/json')) {
    await resolveBlobError(blob)
  }
  const filename = parseFilename(response.headers?.['content-disposition'], fallbackFilename)
  return { blob, filename }
}

export async function createObjectUrlFromResponse(response, fallbackFilename) {
  const { blob, filename } = await normalizeBlobResponse(response, fallbackFilename)
  return {
    filename,
    blob,
    url: window.URL.createObjectURL(blob)
  }
}

export async function saveBlobResponse(response, fallbackFilename) {
  const { blob, filename } = await normalizeBlobResponse(response, fallbackFilename)
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
  return filename
}

export async function openBlobResponse(response, fallbackFilename, previewWindow) {
  const { blob, filename } = await normalizeBlobResponse(response, fallbackFilename)
  const url = window.URL.createObjectURL(blob)
  if (previewWindow && !previewWindow.closed) {
    previewWindow.location.replace(url)
  } else {
    const link = document.createElement('a')
    link.href = url
    link.target = '_blank'
    link.rel = 'noopener'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
  window.setTimeout(() => {
    window.URL.revokeObjectURL(url)
  }, 60000)
  return filename
}
