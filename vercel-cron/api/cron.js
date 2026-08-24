/**
 * Vercel Serverless Function to keep FastAPI backend awake.
 * Triggered automatically by Vercel Cron every 10 minutes.
 */
export default async function handler(req, res) {
  // Target URL (defaults to StepCount Render backend URL or environment variable)
  const backendUrl = process.env.BACKEND_URL || "https://stepcount-backend.onrender.com/health";
  const startTime = Date.now();

  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 25000); // 25s timeout limit

    const response = await fetch(backendUrl, {
      method: "GET",
      headers: {
        "User-Agent": "StepCount-Vercel-KeepAlive/1.0",
        "Accept": "application/json"
      },
      signal: controller.signal
    });

    clearTimeout(timeoutId);

    const durationMs = Date.now() - startTime;
    const statusCode = response.status;
    let responseBody = {};

    try {
      responseBody = await response.json();
    } catch {
      responseBody = { text: await response.text().catch(() => "") };
    }

    console.log(`[KeepAlive] Pinged ${backendUrl} -> HTTP ${statusCode} in ${durationMs}ms`);

    return res.status(200).json({
      success: response.ok,
      targetUrl: backendUrl,
      httpStatus: statusCode,
      durationMs: durationMs,
      timestamp: new Date().toISOString(),
      backendResponse: responseBody
    });
  } catch (error) {
    const durationMs = Date.now() - startTime;
    console.error(`[KeepAlive Error] Failed to ping ${backendUrl}:`, error.message);

    return res.status(500).json({
      success: false,
      targetUrl: backendUrl,
      error: error.message,
      durationMs: durationMs,
      timestamp: new Date().toISOString()
    });
  }
}
