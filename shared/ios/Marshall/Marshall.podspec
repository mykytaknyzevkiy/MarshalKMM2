Pod::Spec.new do |s|
  s.name             = "Marshall"
  s.version          = '1.0.1'
  s.summary          = 'A short description of NekLibary.'

  s.description      = <<-DESC
TODO: Add long description of the pod here.
                       DESC

  s.homepage         = 'https://github.com/nekbakhtzabirov/NekLibary'
  # s.screenshots     = 'www.example.com/screenshots_1', 'www.example.com/screenshots_2'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'nekbakhtzabirov' => 'nekbakht.zabirov@pixelsmatter.com' }
  s.source           = { :git => 'https://github.com/nekbakhtzabirov/NekLibary.git', :tag => s.version.to_s }
  
  s.requires_arc     = true
  s.platform         = :ios, '16.2'
  
  s.swift_version = '5.0'
  
  s.source_files     = 'Marshall/*.{swift,h,m}'
  s.vendored_frameworks = 'libs/*.xcframework'
  end
