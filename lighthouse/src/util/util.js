import fs from 'fs'
import dotenv from 'dotenv'
import Encryptor from 'encryptor-node'

dotenv.config({ path: `${process.cwd()}/.env` })

export function parseJsonIntoObj(path) {
    const browserOptionsStr = fs.readFileSync(path).toString()
    return JSON.parse(browserOptionsStr)
}

export function decrypt(key,encryptedValue) {
    return Encryptor.decrypt(key, encryptedValue)
}