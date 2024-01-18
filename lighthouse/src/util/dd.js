import tracer from 'dd-trace'

tracer.init({ hostname: 'localhost', port: 8126, rateLimit:1})
tracer.dogstatsd.increment('page.value2')
tracer.dogstatsd.increment('page.value2')
tracer.dogstatsd.increment('page.value2')
tracer.dogstatsd.increment('page.value2')
tracer.dogstatsd.increment('page.value2')
tracer.dogstatsd.increment('page.value2')

// tracer.dogstatsd.flush()
