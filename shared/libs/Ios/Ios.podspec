Pod::Spec.new do |spec|
  spec.name                     = 'Ios'
  spec.version                  = '1.2'
  spec.homepage                 = 'Home URL'
  spec.source                   = { :http=> ''}
  spec.authors                  = ''
  spec.license                  = ''
  spec.summary                  = 'This is sample Summary'
  spec.vendored_frameworks      = 'libs/*.*'
  spec.ios.deployment_target    = '14.1'
  spec.swift_version            = '5.0'
  spec.source_files             = 'Ios/*.{swift,h,m}'


  #s.requires_arc     = true
  #s.platform         = :ios, '14.1'

end
