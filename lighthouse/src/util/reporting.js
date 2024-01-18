import fs from 'fs'
import { generateReport } from 'lighthouse';
import { writeMetrics, writeScores } from './influxdb.js';
import { sendHtmlReport, sendReportUrl } from './slack.js';
import { flowConfig } from '../session/session.js';
import { performanceMetrics as metrics } from './constant.js';
import { compareReports } from './compare-reports.js';

const date = new Date().getTime()
const allReportsDir = `${process.cwd()}/report`
export const reportDir = `${allReportsDir}/${date}`
const reportType = `${process.env.REPORT_TYPE}`

export async function writeAggregatedMetrics(aggregatedSessionResult) {
    if (process.env.WRITE_TO_DB) {
        await writeMetricsToInfluxDb(aggregatedSessionResult)
    }
}

export async function generateTestReport(testResult) {
    if (process.env.CREATE_REPORT) {
        createReportDirectories()
        await generateReports(testResult)
    }
}

async function writeMetricsToInfluxDb(aggregatedSessionResult) {
    const { onlyCategories } = flowConfig.config.settings
    const { steps } = aggregatedSessionResult

    await steps.forEach(async step => {
        const { finalDisplayedUrl, gatherMode } = step.lhr
        const modifiedUrl = finalDisplayedUrl.replace(`${process.env.BASE_URL}`, 'base_url/')

        onlyCategories.forEach(async category => {
            if (step.lhr.categories[category]) {
                const { score } = step.lhr.categories[category]
                await writeScores(category, modifiedUrl, score, gatherMode, process.env.PLATFORM)
            }
        })
        metrics.forEach(async metric => {
            if (step.lhr.audits[metric]) {
                const { numericValue } = step.lhr.audits[metric]
                await writeMetrics(metric, modifiedUrl, numericValue, gatherMode,process.env.PLATFORM)
            }
        })
    })
}

async function generateReports(testResult) {
    await generateSummaryReport(testResult)
    // await generateReportForEachStep(result)
}

async function generateSummaryReport(testResult) {
    const reportPath = `${reportDir}/summary-${date}.${reportType}`
    const summaryReport = generateReport(testResult, reportType)
    fs.writeFileSync(reportPath, summaryReport);

    const report = {
        path: reportPath,
        comment: 'Test run complete.',
        title: `summary-${date}.${reportType}`
    }
    await sendReportSummary(report)
}

async function generateReportForEachStep(result) {
    const { steps } = result
    await steps.forEach(async step => {
        const stepReport = generateReport(step.lhr, reportType)
        const { finalDisplayedUrl } = step.lhr
        const reportName = finalDisplayedUrl.replace(/\//g, ".")
        fs.writeFileSync(`${reportDir}/${reportName}.${reportType}`, stepReport)
    })
}

async function sendReportSummary(report) {
    if (process.env.SEND_GRAFANA_LINK) {
        await sendReportUrl(`${process.env.GRAFANA_LINK}`)
    }
    if (process.env.SEND_REPORT_LINK) {
        const reportUrl = `${process.env.NGINX_HOST}/report/${date}/${report.title}`
        await sendReportUrl(reportUrl)
    }
    if (process.env.SEND_HTML_REPORT) {
        await sendHtmlReport(report)
    }
    if (process.env.SEND_COMPARE_REPORT_URL) {
        compareReports()
        const compareReportUrl = `${process.env.WORK_SPACE}/report/0/compare-report.html`
        await sendReportUrl(compareReportUrl)
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
