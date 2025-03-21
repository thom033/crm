### 1. Bug lors du partage d'un fichier docs rehefa atao full control

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <!-- Tell the browser to be responsive to screen width -->
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    <!-- Favicon icon -->
    <link rel="icon" type="image/png" sizes="16x16" href="../assets/images/favicon.png">
    <title>Elite Admin Template - The Ultimate Multipurpose admin template</title>
    <!-- Custom CSS -->
    <link href="/css/style.min.css" rel="stylesheet">
    <!-- page css -->
    <link href="/css/pages/error-pages.css" rel="stylesheet">
    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
    <script src="https://oss.maxcdn.com/libs/respond.js/1.4.2/respond.min.js"></script>
    <![endif]-->
</head>
<body class="skin-blue fixed-layout">
<!-- ============================================================== -->
<!-- Main wrapper - style you can find in pages.scss -->
<!-- ============================================================== -->
    <section id="wrapper" class="error-page">
        <div class="error-box">
            <div class="error-body text-center">
                <h1>500</h1>
                <h3 class="text-uppercase">Internal Server Error !</h3>
                <p class="text-muted m-t-30 m-b-30">Please try after some time</p>
                <a href="/" class="btn btn-info btn-rounded waves-effect waves-light m-b-40">Back to home</a>
            </div>
        </div>
    </section>
    <script> var home = "/"; </script>
    <script src="/js/library/jquery-3.2.1.min.js" type="text/javascript"></script>
    <script src="/js/library/popper.min.js" type="text/javascript"></script>
    <script src="/js/library/bootstrap.min.js" type="text/javascript"></script>
    <script src="/js/library/waves.js" type="text/javascript"></script>
</body>
</html>
```

### 2. bug lors de la creation d'un new lead 
    => Erreur 404
    Tsy misy erreur any @ terminal fa tonga de erreur 404 no ao @ navigateur

### 3. Tsy tafiditra mijery ny leads any ny clients 
    ito no lien => http://localhost:8080/customer/my-leads
    erreur 

    Caused by: org.attoparser.ParseException: Could not parse as expression: "${home + 'customer/lead/' + ${lead.leadId}" (template: "customer-info/my-leads" - line 99, col 48)
        at org.attoparser.MarkupParser.parseDocument(MarkupParser.java:393) ~[attoparser-2.0.6.RELEASE.jar:2.0.6.RELEASE]
        at org.attoparser.MarkupParser.parse(MarkupParser.java:257) ~[attoparser-2.0.6.RELEASE.jar:2.0.6.RELEASE]
        at org.thymeleaf.templateparser.markup.AbstractMarkupTemplateParser.parse(AbstractMarkupTemplateParser.java:230) ~[thymeleaf-3.1.1.RELEASE.jar:3.1.1.RELEASE]
        ... 95 common frames omitted
    Caused by: org.thymeleaf.exceptions.TemplateProcessingException: Could not parse as expression: "${home + 'customer/lead/' + ${lead.leadId}" (template: "customer-info/my-leads" - line 99, col 48)

### 4. rehefa mcree lead dia tsy mety apina file 
    2025-03-21T14:38:58.393+03:00 ERROR 4076 --- [nio-8080-exec-7] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.dao.DataIntegrityViolationException: could not execute statement [Data truncation: Data too long for column 'file_data' at row 1] [insert into file (contract_id,file_data,file_name,file_type,lead_id) values (?,?,?,?,?)]; SQL [insert into file (contract_id,file_data,file_name,file_type,lead_id) values (?,?,?,?,?)]] with root cause

    com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column 'file_data' at row 1
