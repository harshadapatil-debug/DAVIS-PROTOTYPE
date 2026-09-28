"""Single AIML service boundary.

CLI:      python -m aiml.api.service --input data/synthetic_intelligence.json [--output out.json]
HTTP:     python -m aiml.api.service --serve [--port 8090]
          POST /aiml/analyze   body = AIML input JSON  ->  unified AIML output JSON
          GET  /aiml/health
"""
import argparse
import json
import logging
import sys
from http.server import BaseHTTPRequestHandler, HTTPServer

from ..contract import ContractError
from .pipeline import run_pipeline


class _Handler(BaseHTTPRequestHandler):
    def _send(self, status, body):
        raw = json.dumps(body).encode()
        self.send_response(status)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(raw)))
        self.end_headers()
        self.wfile.write(raw)

    def do_GET(self):
        if self.path == "/aiml/health":
            self._send(200, {"status": "UP"})
        else:
            self._send(404, {"error": "NOT_FOUND"})

    def do_POST(self):
        if self.path != "/aiml/analyze":
            return self._send(404, {"error": "NOT_FOUND"})
        try:
            payload = json.loads(self.rfile.read(int(self.headers.get("Content-Length", 0))) or b"{}")
            self._send(200, run_pipeline(payload))
        except (ValueError, json.JSONDecodeError) as exc:
            self._send(400, {"error": "BAD_REQUEST", "message": str(exc)})
        except ContractError as exc:
            self._send(500, {"error": "INTERNAL_SERVER_ERROR", "message": str(exc)})

    def log_message(self, *args):
        pass


def main(argv=None):
    ap = argparse.ArgumentParser(description="DAVIS unified AIML pipeline")
    ap.add_argument("--input")
    ap.add_argument("--output")
    ap.add_argument("--serve", action="store_true")
    ap.add_argument("--port", type=int, default=8090)
    args = ap.parse_args(argv)
    logging.basicConfig(level=logging.INFO, stream=sys.stderr)
    if args.serve:
        HTTPServer(("127.0.0.1", args.port), _Handler).serve_forever()
        return 0
    if not args.input:
        ap.error("--input or --serve required")
    with open(args.input, encoding="utf-8") as fh:
        result = run_pipeline(json.load(fh))
    text = json.dumps(result, indent=2)
    if args.output:
        with open(args.output, "w", encoding="utf-8") as fh:
            fh.write(text)
    else:
        print(text)
    return 0


if __name__ == "__main__":
    sys.exit(main())
