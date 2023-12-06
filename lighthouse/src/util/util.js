import fs from 'fs'
import dotenv from 'dotenv'
import { expand } from 'dotenv-expand';
import { generateReport } from 'lighthouse';
import { writeMetricsToInflux, writeCategoryScoresToInflux } from './influxdb.js';
import { sendHtmlReport, sendReportUrl } from './slack.js';

export const envConfig = dotenv.config({ path: `${process.cwd()}/.env`, override: true });
expand(envConfig);

const date = new Date().getTime()
const allReportsDir = `${process.cwd()}/report`
const reportDir = `${allReportsDir}/${date}`
const reportType = `${process.env.REPORT_TYPE}`

export async function generateReportWriteMetrics(flow) {
    const result = await flow.createFlowResult()

    if (process.env.WRITE_TO_DB) {
        await writeMetricsToInfluxDb(result)
    }

    createReportDirectories()
    await generateReports(result)
}

async function writeMetricsToInfluxDb(result) {
    const categories = ['performance', 'best-practices', 'accessibility', 'seo']
    const metrics = ['first-contentful-paint', 'total-blocking-time', 'cumulative-layout-shift', 'largest-contentful-paint']
    const steps = result.steps

    await steps.forEach(async step => {
        const url = step.lhr.finalUrl
        categories.forEach(async category => {
            const categoryScore = step.lhr.categories[category].score
            await writeCategoryScoresToInflux('navigation-audit', category, url, categoryScore)
        })
        metrics.forEach(async metric => await writeMetricsToInflux('performance', metric, url, step.lhr.audits[metric].numericValue))
    })
}

async function generateReports(result) {
    await generateSummaryReport(result)
    await generateReportForEachStep(result)
}

async function generateSummaryReport(result) {
    const reportPath = `${reportDir}/summary-${date}.${reportType}`
    const summaryReport = generateReport(result, reportType)
    fs.writeFileSync(reportPath, summaryReport);

    const report = {
        path: reportPath,
        comment: 'Test run complete.',
        title: `summary-${date}.${reportType}`
    }

    await sendReportSummary(report)
}

async function generateReportForEachStep(result) {
    const steps = result.steps
    await steps.forEach(async step => {
        const stepReport = generateReport(step.lhr, reportType)
        const url = step.lhr.finalUrl
        const reportName = url.replace(/\//g, ".")
        fs.writeFileSync(`${reportDir}/${reportName}.${reportType}`, stepReport)
    })
}

async function sendReportSummary(report) {
    if (process.env.SEND_REPORT_LINK) {
        const reportUrl = `${process.env.NGINX_HOST}/report/${date}/${report.title}`
        await sendReportUrl(reportUrl)
    }
    if (process.env.SEND_HTML_REPORT) {
        await sendHtmlReport(report)
    }
}

function createReportDirectories() {
    if (!fs.existsSync(allReportsDir)) {
        fs.mkdirSync(allReportsDir)
    }
    if (!fs.existsSync(reportDir)) {
        fs.mkdirSync(reportDir)
    }
}