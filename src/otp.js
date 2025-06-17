import { createHmac } from "node:crypto";

/**
 * @param {string} hex
 * @returns {number[]}
 */
function hexToBytes(hex) {
	return (hex.match(/.{1,2}/g) ?? []).map((char) => Number.parseInt(char, 16));
}

/**
 * @param {string} message
 * @param {string} key
 * @returns {string}
 */
function computeHmacSha1(message, key) {
	const hmac = createHmac("sha1", Buffer.from(base32toHex(key), "hex"));
	hmac.update(Buffer.from(message, "hex"));
	return hmac.digest("hex");
}

/**
 * @param {string} base32
 * @returns {string}
 */
function base32toHex(base32) {
	const base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

	const bits = base32
		.toUpperCase() // Since base 32, we coerce lowercase to uppercase
		.replace(/=+$/, "")
		.split("")
		.map((value) => base32Chars.indexOf(value).toString(2).padStart(5, "0"))
		.join("");

	const hex = (bits.match(/.{1,8}/g) ?? [])
		.map((chunk) => Number.parseInt(chunk, 2).toString(16).padStart(2, "0"))
		.join("");

	return hex;
}

/**
 * @param {Objecxt} args
 * @param {string} key
 * @param {number} [counter=0]
 * @returns {string}
 */
function generateHOTP({ key, counter = 0 }) {
	// Compute HMACdigest
	const digest = computeHmacSha1(counter.toString(16).padStart(16, "0"), key);

	// Get byte array
	const bytes = hexToBytes(digest);

	// Truncate
	const offset = bytes[19] & 0xf;
	const v =
		((bytes[offset] & 0x7f) << 24) |
		((bytes[offset + 1] & 0xff) << 16) |
		((bytes[offset + 2] & 0xff) << 8) |
		(bytes[offset + 3] & 0xff);

	const code = String(v % 1000000).padStart(6, "0");

	return code;
}

/**
 * @param {Object} args
 * @param {number} args.now
 * @param {number} args.timeStep
 * @returns {number}
 */
function getCounterFromTime({ now, timeStep }) {
	return Math.floor(now / 1000 / timeStep);
}

/**
 * @param {Object} args
 * @param {string} args.key
 * @param {number} [now=Date.now()]
 * @param {number} [timeStep=30]
 * @returns {string}
 */
export function generateTOTP({ key, now = Date.now(), timeStep = 30 }) {
	const counter = getCounterFromTime({ now, timeStep });

	return generateHOTP({ key, counter });
}
